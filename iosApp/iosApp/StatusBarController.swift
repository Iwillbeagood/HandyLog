import UIKit

class StatusBarHostingController: UIViewController {
    private let childVC: UIViewController
    private var isDarkTheme: Bool = false

    init(childVC: UIViewController) {
        self.childVC = childVC
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(childVC)
        childVC.view.frame = view.bounds
        childVC.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(childVC.view)
        childVC.didMove(toParent: self)

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(updateStatusBar(_:)),
            name: Notification.Name("UpdateStatusBarStyle"),
            object: nil
        )
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        isDarkTheme ? .lightContent : .darkContent
    }

    @objc private func updateStatusBar(_ notification: Notification) {
        if let userInfo = notification.userInfo,
           let dark = userInfo["isDarkTheme"] as? Bool {
            isDarkTheme = dark
        }
        setNeedsStatusBarAppearanceUpdate()
    }
}
