//
//  ViewController.swift
//  TestKtor
//
//  Created by fabio.bombardi on 18/12/21.
//

import UIKit

import kotlin_mpp

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        let check = Sample().checkMe()
        print("Checked! \(check)")
    }


}

