import Foundation
import UIKit
import VIDVLiveness

@objc class VIDVLivenessInitializer: NSObject, VIDVLivenessDelegate {
    
    var jsonResponse = ""
    var success = false
    fileprivate var returnDataToJS:Observable<Bool> = Observable(false)
    
    // function to start valify
    @objc func startLiveness(sender: UIViewController, argArr: [AnyObject], completion: @escaping (Bool, String) -> ()) {
        
        let arg = argArr[0] as? NSDictionary ?? NSDictionary()
        let baseURL = arg.value(forKey: "base_url") as? String ?? ""
        let accessToken = arg.value(forKey: "access_token") as? String ?? ""
        let bundle = arg.value(forKey: "bundle_key") as? String ?? ""
        
        let enableSmile  = arg.value(forKey: "enable_smile") as? Bool ?? true
        let enableLeft  = arg.value(forKey: "enable_look_left") as? Bool ?? true
        let enableRight  = arg.value(forKey: "enable_look_right") as? Bool ?? true
        let enableEyes  = arg.value(forKey: "enable_close_eyes") as? Bool ?? true
        
        let languageString = arg.value(forKey: "language") as? String ?? ""
        let trials  = arg.value(forKey: "livenss_number_of_failed_trials") as? Int
        let timer  = arg.value(forKey: "liveness_time_per_action") as? Int
        let instructions  = arg.value(forKey: "liveness_number_of_instructions") as? Int
        
        let facematchOcrTransactionId  = arg.value(forKey: "facematch_ocr_transactionId") as? String
        let enableVoiceover = arg.value(forKey: "enable_voiceover") as? Bool ?? true
        let showErrorMessage = arg.value(forKey: "show_error_message") as? Bool ?? true
        let primaryColor  = arg.value(forKey: "primary_color") as? String ?? ""
        
        let facematchImage = argArr[1] as? String
        let headers  = argArr[2] as? [String:String] ?? [:]
        
        // creds
        var settings = VIDVLivenessBuilder()
            .setBundleKey(bundle)
            .setAccessToken(accessToken)
            .setBaseURL(baseURL)
        
        // liveness actions
        if !enableEyes {
            settings = settings.withoutCloseEyes()
        }
        if !enableLeft {
            settings = settings.withoutLookLeft()
        }
        if !enableRight {
            settings = settings.withoutLookRight()
        }
        if !enableSmile {
            settings = settings.withoutSmile()
        }
        
        // liveness settings
        if languageString != "" {
            settings = settings.setLanguage(languageString)
        }
        if let trials = trials {
            settings = settings.setFailTrials(trials)
        }
        if let timer = timer {
            settings = settings.setInstructionTimer(timer)
        }
        if let instructionsCount = instructions {
            settings = settings.setNumberOfInstructions(instructionsCount)
        }
        
        // generic settings
        if !enableVoiceover {
            settings = settings.withoutVoiceOver()
        }
        if primaryColor != "" {
            let customColor = hexStringToUIColor(hex: primaryColor)
            settings = settings.setPrimaryColor(customColor)
        }
        if !headers.isEmpty {
            settings = settings.setHeaders(headers)
        }
        settings = settings.showErrorDialogs(showErrorMessage)
        
        // face match
        if let facematchOcrTransactionId = facematchOcrTransactionId  {
            settings = settings.setFrontTransactionID(facematchOcrTransactionId)
        } else if let facematchImage = facematchImage {
            let data = Data(base64Encoded: facematchImage ) ?? Data()
            let image = UIImage(data: data)
            settings = settings.setFaceMatchImage(image)
        }
        
        settings.start(vc: sender, livenessDelegate: self)
        
        returnDataToJS.observe { send in
            if send {
                completion(self.success, self.jsonResponse)
            }
        }
    }
}

extension VIDVLivenessInitializer {
    
    func onLivenessResult(_ VIDVLivenessResponse: VIDVLiveness.VIDVLivenessResponse) {
        let dict = NSMutableDictionary()
        let nameValuePairs = NSMutableDictionary()
        dict["nameValuePairs"] = nameValuePairs
        
        switch VIDVLivenessResponse {
        case .success(VIDVLivenessResult: let result):
            let VIDVReslut = encodeLivenessResults(VIDVResult: result)
            nameValuePairs["state"] = "SUCCESS"
            nameValuePairs["livenessResult"] = VIDVReslut
            success = true
            returnData(dict: dict)
        case .serviceFailure(VIDVLivenessResult: let result, errorCode: let errorCode, errorMessage: let errorMessage):
            let VIDVReslut = encodeLivenessResults(VIDVResult: result)
            nameValuePairs["state"] = "FAILURE"
            nameValuePairs["errorCode"] = errorCode
            nameValuePairs["errorMessage"] = errorMessage
            nameValuePairs["livenessResult"] = VIDVReslut
            success = false
            returnData(dict: dict)
        case .builderError(errorCode: let errorCode, errorMessage: let errorMessage):
            nameValuePairs["state"] = "ERROR"
            nameValuePairs["errorCode"] = errorCode
            nameValuePairs["errorMessage"] = errorMessage
            success = false
            returnData(dict: dict)
        case .userExited(VIDVLivenessResult: let result, step: let step):
            let VIDVReslut =  encodeLivenessResults(VIDVResult: result)
            nameValuePairs["state"] = "EXIT"
            nameValuePairs["step"] = step
            nameValuePairs["livenessResult"] = VIDVReslut
            success = false
            returnData(dict: dict)
        case .capturedActions(capturedActions: let capturedActions):
            encodeCapturedImages(capturedAction: capturedActions)
        @unknown default:
            break;
        }
    }
    
    func encodeCapturedImages(capturedAction: VIDVDetectedFace) {
        do {
            let dict = NSMutableDictionary()
            let nameValuePairs = NSMutableDictionary()
            dict["nameValuePairs"] = nameValuePairs
            nameValuePairs["state"] = "CAPTURED_IMAGES"
            let jsonEncoder = JSONEncoder()
            let jsonData = try jsonEncoder.encode(capturedAction)
            let json = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String : Any]
            nameValuePairs["capturedImage"] = json
            success = true
            returnData(dict: dict)
        } catch {
            print("Failed to encode VIDV Result: \(error.localizedDescription)")
        }
    }
    
    func encodeLivenessResults(VIDVResult: VIDVLivenessResult?) -> [String: Any] {
        if let VIDVResult = VIDVResult {
            do {
                let jsonEncoder = JSONEncoder()
                let jsonData = try jsonEncoder.encode(VIDVResult)
                let json = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String : Any]
                return json ?? [:]
            } catch {
                print("Failed to encode VIDV Result: \(error.localizedDescription)")
                return [:]
            }
        } else {
            return [:]
        }
    }
    
    func returnData(dict: NSMutableDictionary){
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dict, options: .prettyPrinted)
            let json = String(data: jsonData, encoding: .utf8)
            self.jsonResponse = json ?? ""
            returnDataToJS.value = true
        } catch let error as NSError {
            print("Failed to load: \(error.localizedDescription)")
        }
    }
}


// helpers
extension VIDVLivenessInitializer {
    func hexStringToUIColor(hex: String?) -> UIColor {
        guard let hex = hex  else {return hexStringToUIColor(hex: "#62CBC9")}
        var cString: String = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        
        if cString.hasPrefix("#") {
            cString.remove(at: cString.startIndex)
        }
        
        if (cString.count) != 6 {
            return UIColor.gray
        }
        
        var rgbValue: UInt64 = 0
        Scanner(string: cString).scanHexInt64(&rgbValue)
        
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
}

fileprivate class Observable<T> {
    typealias Listener = (T) -> Void
    var listener: Listener?
    
    var value: T {
        didSet {
            listener?(value)
        }
    }
    
    init(_ v: T) {
        value = v
    }
    
    func bind(_ listener: Listener?) {
        self.listener = listener
    }
    
    func observe(_ listener: Listener?) {
        self.listener = listener
        listener?(value)
    }
}