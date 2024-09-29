import UIKit
import VIDVOCR

@objc class VIDVOCRInitializer: NSObject, VIDVOCRDelegate {
    fileprivate var returnDataToJS:Observable<Bool> = Observable(false)
    var jsonResponse = ""
    var success = false
    var enableLogging = false
    var events = [[String: Any]]() // To store event logs
    var errors = [[String: Any]]() // To store error logs
    var capturedImages = [CapturedImageData]() // To store captured images
    
    // Function to handle OCR results
    func onOCRResult(result: VIDVOCRResponse) {
        var dict = NSMutableDictionary()
        switch result {
        case .success(let data):
            dict = VIDVResultToDict(VIDVResult: data, hasResult: true, errorMessage: nil, errorCode: nil, step: nil, state: "SUCCESS")
            success = true
        case .builderError(let code, let message):
            dict = VIDVResultToDict(VIDVResult: nil, hasResult: false, errorMessage: message, errorCode: code, step: nil, state: "ERROR")
            logError(code: code, message: message) // Log the error
            success = false
        case .serviceFailure(let code, let message, let data):
            dict = VIDVResultToDict(VIDVResult: data, hasResult: true, errorMessage: message, errorCode: code, step: nil, state: "FAILURE")
            logError(code: code, message: message) // Log the error
            success = false
        case .userExit(let step, let data):
            dict = VIDVResultToDict(VIDVResult: data, hasResult: true, errorMessage: nil, errorCode: nil, step: step, state: "EXIT")
            success = false
        case .capturedImages(let capturedImageData):
            capturedImages.append(capturedImageData) // Store captured image data
            return
        @unknown default:
            return
        }
        serializeAndRespond(dict: dict)
    }
    
    // Convert OCR results to dictionary
    func VIDVResultToDict(VIDVResult: VIDVOCRResult?, hasResult: Bool, errorMessage: String?, errorCode: Int?, step: String?, state: String) -> NSMutableDictionary {
        let dict = NSMutableDictionary()
        let nameValuePairs = NSMutableDictionary()
        
        dict["nameValuePairs"] = nameValuePairs
        nameValuePairs["state"] = state
        
        if let errorCode = errorCode {
            nameValuePairs["errorCode"] = errorCode
        }
        if let errorMessage = errorMessage {
            nameValuePairs["errorMessage"] = errorMessage
        }
        if let step = step {
            nameValuePairs["step"] = step
        }
        
        if hasResult {
            do {
                let jsonEncoder = JSONEncoder()
                let jsonData = try jsonEncoder.encode(VIDVResult)
                let jsonResult = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String : Any]
                nameValuePairs["ocrResult"] = jsonResult
                
                let capturedImagesData = try jsonEncoder.encode(capturedImages)
                let capturedImagesResult = try JSONSerialization.jsonObject(with: capturedImagesData, options: []) as? [[String : Any]]
                nameValuePairs["capturedImages"] = capturedImagesResult
                
                if enableLogging {
                    nameValuePairs["events"] = events
                    nameValuePairs["errors"] = errors
                }
            } catch {
                print("Failed to encode VIDV Result: \(error.localizedDescription)")
            }
        }
        
        return dict
    }
    
    // Log errors
    func logError(code: Int, message: String) {
        if enableLogging {
            errors.append(["errorCode": code, "errorMessage": message])
        }
    }
    
    // Serialize and respond to JS
    func serializeAndRespond(dict: NSMutableDictionary) {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dict, options: .prettyPrinted)
            let json = String(data: jsonData, encoding: .utf8)
            self.jsonResponse = json ?? ""
            returnDataToJS.value = true
        } catch let error as NSError {
            print("Failed to load: \(error.localizedDescription)")
        }
    }
    
    // Function to start OCR
    @objc func startOCR(sender: UIViewController, argArr: [NSDictionary], completion: @escaping (Bool, String) -> ()) {
        let arg = argArr.first!
        let baseURL = arg.value(forKey: "base_url") as? String ?? ""
        let accessToken = arg.value(forKey: "access_token") as? String ?? ""
        let bundle = arg.value(forKey: "bundle_key") as? String ?? ""
        let languageString = arg.value(forKey: "language") as? String ?? ""
        let documentVerification  = arg.value(forKey: "document_verification") as? Bool ?? false
        let dataValidation  = arg.value(forKey: "data_validation") as? Bool ?? false
        let returnDataValidationError  = arg.value(forKey: "return_data_validation_error") as? Bool ?? false
        let reviewData  = arg.value(forKey: "review_data") as? Bool ?? false
        let captureOnlyMode  = arg.value(forKey: "capture_only_mode") as? Bool ?? false
        let manualCaptureMode  = arg.value(forKey: "manual_capture_mode") as? Bool ?? false
        let previewCapturedImage  = arg.value(forKey: "preview_captured_image") as? Bool ?? false
        let headers  = arg.value(forKey: "headers") as? [String:String] ?? [:]
        let primaryColor = arg.value(forKey: "primary_color") as? String ?? ""
        self.enableLogging  = arg.value(forKey: "enable_logging") as? Bool ?? false
        
        DispatchQueue.main.async {[weak self] in
            guard let self = self else {return}
            var builder = OCRBuilder()
            builder = builder.setDataValidation(validate: dataValidation)
                .setDocumentVerification(verify: documentVerification)
                .setlLanguage(language: languageString)
                .setBundleKey(bundle)
                .setBaseUrl(baseURL)
                .setAccessToken(accessToken)
                .setReviewData(review: reviewData)
                .setCaptureOnlyMode(captureOnlyMode)
                .setReturnValidationError(returnDataValidationError)
                .setManualCaptureMode(manualCaptureMode:manualCaptureMode)
                .setPreviewCapturedImage(previewCapturedImage)
            builder = builder.setPrimaryColor(color: self.hexStringToUIColor(hex: primaryColor))
            builder = builder.setHeaders(headers: headers)
            if self.enableLogging {
                builder = builder.setLogsDelegate(self)
            }
            builder.start(vc: sender, ocrDelegate: self)
        }
        
        returnDataToJS.observe { send in
            if send {
                self.returnDataToJS.value = false
                completion(self.success, self.jsonResponse)
            }
        }
    }
    
    // Convert hex string to UIColor
    func hexStringToUIColor(hex: String?) -> UIColor {
        guard let hex = hex else { return hexStringToUIColor(hex: "#62CBC9") }
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

// Observable class to handle value changes
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

// Extension to handle logging
extension VIDVOCRInitializer: VIDVLogsDelegate {
    func onOCRLog(log: VIDVOCR.VIDVEvent) {
        if enableLogging {
            do {
                let jsonEncoder = JSONEncoder()
                let jsonData = try jsonEncoder.encode(log)
                if let jsonResult = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String : Any] {
                    events.append(jsonResult)
                }
            } catch {
                print("Failed to encode VIDV Result: \(error.localizedDescription)")
            }
        }
    }
    
    func onOCRLog(log: VIDVOCR.VIDVError) {
        if enableLogging {
            do {
                let jsonEncoder = JSONEncoder()
                let jsonData = try jsonEncoder.encode(log)
                if let jsonResult = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String : Any] {
                    errors.append(jsonResult)
                }
            } catch {
                print("Failed to encode VIDV Result: \(error.localizedDescription)")
            }
        }
    }
}
