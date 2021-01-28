#
#  Be sure to run `pod spec lint RCTWeChat.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#
require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RCTWeChat"
  s.version      = package["version"]
  s.summary      = "React-Native(iOS/Android) functionalities include WeChat Login, Share, Favorite and Payment {QQ: 336021910}"
  s.description  = <<-DESC
  React-Native(iOS/Android) functionalities include WeChat Login, Share, Favorite and Payment {QQ: 336021910}
   DESC
  s.author       = package["author"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/liangfengsid/react-native-wechat.git", :tag => "master" }
  s.source_files  = "ios/*.{h,m}"
  s.dependency "React"
  s.vendored_libraries = "ios/libWeChatSDK.a"
  s.vendored_frameworks = "ios/TencentOpenAPI.framework","ios/DTShareKit.framework"
  s.requires_arc = true
  s.frameworks = 'SystemConfiguration','CoreTelephony'
  s.library = 'sqlite3','c++','z'
end
