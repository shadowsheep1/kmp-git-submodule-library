package ktor

import io.ktor.client.engine.*
import io.ktor.client.features.*
import io.ktor.util.*
import it.shadowsheep.kotlin.mpp.app.client.apis.DefaultApi
import it.shadowsheep.kotlin.mpp.app.client.infrastructure.HttpResponse
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal expect val ApplicationDispatcher: CoroutineContext

open class CoroutinePresenter(
    private val mainContext: CoroutineContext = ApplicationDispatcher
) : CoroutineScope {

    private val job = Job()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        print(throwable)
    }

    override val coroutineContext: CoroutineContext
        get() = mainContext + job + exceptionHandler

    open fun cancelRequest() {
        cancel()
    }

    open fun onDestroy() {
        print("CoroutinePresenter onDestroy: cancel job!")
        job.cancel()
    }
}

expect fun clientEngine(timeout: Int = 5): HttpClientEngine

class ApiService(
    timeout: Int = 5,
    staging: Boolean = false,
    test: Boolean = false,
    devBranch: String = ""
) {
    val defaultApi = DefaultApi(
        httpClientEngine = clientEngine(timeout),
        jsonSerializer = Json {
            isLenient = true
            ignoreUnknownKeys = true
            // https://github.com/Kotlin/kotlinx.serialization/issues/1450
            useAlternativeNames = false
        }
    )
}

expect class WeakRef<T : Any>(referred: T) {
    fun clear()
    fun get(): T?
}

@Suppress("UNUSED")
open class BasePresenter(
    apiService: ApiService
) : CoroutinePresenter() {
    open val service: WeakRef<ApiService> = WeakRef(apiService)

    open val jsonSerilizer = Json {
        isLenient = true
    }

    open class BaseRequestViewState(
        val apiErrorMessage: String? = null,
        val statusCode: Int? = null,
        val unexpectedError: String? = null,
        val indicatorAnimating: Boolean = false,
        val requestSucceeded: Boolean = false,
        val validationSucceeded: Boolean = false
    )

    open suspend fun <T : BaseRequestViewState, M : Any> requestAsync(
        returnT: (BaseRequestViewState) -> T,
        initialViewStateHandler: (T) -> Unit,
        retrieveFromDb: (() -> T?)? = null,
        apiCall: suspend (ApiService) -> HttpResponse<M>,
        successfulHandler: (M) -> T,
        validationCall: (() -> T)? = null
    ): T {
        validationCall?.let {
            val validationViewState = it()
            if (!validationViewState.validationSucceeded) {
                return validationViewState
            }
        }

        initialViewStateHandler(
            returnT(
                BaseRequestViewState(
                    indicatorAnimating = true
                )
            )
        )

        retrieveFromDb?.let {
            val dbViewState = it()
            dbViewState?.let {
                return it
            }
        }

        return try {
            // OKAY - MAKE SINGLETON
            if (service.get() == null) {
                returnT(
                    BaseRequestViewState(
                        requestSucceeded = false,
                        unexpectedError = "Weak reference service is null|"
                    )
                )
            }
            val service = this.service.get()!!
            val response = apiCall(service)
            val statusCode = response.status
            val headers = response.headers
            print("headers: $headers")

            println("--> K-side Status code: $statusCode")

            when (statusCode) {
                200 -> {
                    // only here we return the actual T class
                    return successfulHandler(response.body())
                }
                else -> {
                    returnT(
                        BaseRequestViewState(
                            apiErrorMessage = "Unexpected error for status code: $statusCode",
                            requestSucceeded = false,
                            statusCode = statusCode
                        )
                    )
                }
            }
        } catch (cre: ClientRequestException) { // 400..499
            val headers = cre.response.headers.toMap()
            val statusCode = cre.response.status.value
            handleManagedServerError(headers, statusCode, returnT)
        } catch (sre: ServerResponseException) { // 500..599
            val statusCode = sre.response.status.value
            val message = "Errore del server: $statusCode"
            handleKtorResponseException(returnT, message)
        } catch (rre: RedirectResponseException) { // 300..399
            val statusCode = rre.response.status.value
            val message = "Errore di redirect: $statusCode"
            handleKtorResponseException(returnT, message)
        } catch (re: ResponseException) {
            val statusCode = re.response.status.value
            val message = "Errore generico di risposta: $statusCode"
            handleKtorResponseException(returnT, message)
        } catch (exception: Exception) {
            returnT(
                BaseRequestViewState(
                    apiErrorMessage = exception.message ?: ""
                )
            )
        } catch (throwable: Throwable) {
            returnT(
                BaseRequestViewState(
                    apiErrorMessage = "Exception"
                )
            )
        }
    }

    private fun <T : BasePresenter.BaseRequestViewState> handleKtorResponseException(
        returnT: (BasePresenter.BaseRequestViewState) -> T,
        message: String
    ) = returnT(
        BasePresenter.BaseRequestViewState(
            apiErrorMessage = "Api error!"
        )
    )

    private fun <T : BaseRequestViewState> handleManagedServerError(
        headers: Map<String, List<String>>,
        statusCode: Int,
        returnT: (BaseRequestViewState) -> T
    ): T {
        return returnT(
            BaseRequestViewState(
                apiErrorMessage = "Error!",
                statusCode = statusCode
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        println("K side - ${this::class} onDestroy!")
        this.service.clear()
    }
}

class StatusPresenter(
    apiService: ApiService
) : BasePresenter(apiService) {

    class StatusRequestViewState(
        apiErrorMessage: String? = null,
        statusCode: Int? = null,
        unexpectedError: String? = null,
        indicatorAnimating: Boolean = false,
        requestSucceeded: Boolean = false,
        val status: String? = null,
    ) : BaseRequestViewState(
        apiErrorMessage,
        statusCode,
        unexpectedError,
        indicatorAnimating,
        requestSucceeded
    ) {
        constructor(viewState: BaseRequestViewState) : this(
            apiErrorMessage = viewState.apiErrorMessage,
            statusCode = viewState.statusCode,
            unexpectedError = viewState.unexpectedError,
            indicatorAnimating = viewState.indicatorAnimating,
            requestSucceeded = viewState.requestSucceeded
        )
    }

    //region getStatus
    fun getStatus(
        initialViewStateHandler: (StatusRequestViewState) -> Unit,
        completion: (StatusRequestViewState) -> Unit,
    ) {
        launch {
            val viewState = requestAsync(
                returnT = {
                    StatusRequestViewState(
                        viewState = it
                    )
                },
                initialViewStateHandler = initialViewStateHandler,
                apiCall = {
                    it.defaultApi.getStatus()
                },
                successfulHandler = {
                    StatusRequestViewState(
                        requestSucceeded = true,
                        status = it.status
                    )
                }
            )
            completion(viewState)
        }
    }
    //endregion
}