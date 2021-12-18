//
//  ViewController.swift
//  TestKtor
//
//  Created by fabio.bombardi on 18/12/21.
//

import UIKit

import kotlin_mpp

class ViewController: UIViewController {
    lazy var apiService: ApiService = {
        return ApiService(
            timeout: 5
        )
    }()
    
    private lazy var statusPresenter: StatusPresenter? = StatusPresenter(apiService: apiService)
    
    @IBOutlet weak var console: UITextView!
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        let check = Sample().checkMe()
        print("Checked! \(check)")
    }

    @IBAction func callApiAction(_ sender: Any) {
        statusPresenter?.getStatus(
            initialViewStateHandler: { [weak self] viewState in
                guard let _ = self else { return }
                print("Do nothing")
            },
            completion: { [weak self] viewState in
                guard let self = self else { return }
                var error = viewState.unexpectedError ?? viewState.apiErrorMessage ?? ""
                print("Error: \(error)")
                let status = viewState.status ?? ""
                print("Status: \(status)")
                self.console.insertText("\(error)\(status)\n")
            }
        )
    }
    
    deinit {
        statusPresenter?.onDestroy()
        statusPresenter = nil
    }
    
}

