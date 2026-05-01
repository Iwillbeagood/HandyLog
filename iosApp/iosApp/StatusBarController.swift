import UIKit

class StatusBarHostingController: UIViewController {
    private let childVC: UIViewController
    private var isDarkTheme: Bool = false
    private let statusBarBackgroundView: UIView = {
        let view = UIView()
        view.isUserInteractionEnabled = false
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private let navigationBarBackgroundView: UIView = {
        let view = UIView()
        view.isUserInteractionEnabled = false
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()

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

        // 키보드 등장/퇴장 시 뷰 리사이즈 방지 — Compose가 자체 처리
        childVC.view.insetsLayoutMarginsFromSafeArea = false

        view.addSubview(statusBarBackgroundView)
        NSLayoutConstraint.activate([
            statusBarBackgroundView.topAnchor.constraint(equalTo: view.topAnchor),
            statusBarBackgroundView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            statusBarBackgroundView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            statusBarBackgroundView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
        ])

        view.addSubview(navigationBarBackgroundView)
        NSLayoutConstraint.activate([
            navigationBarBackgroundView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            navigationBarBackgroundView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            navigationBarBackgroundView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            navigationBarBackgroundView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(updateStatusBar(_:)),
            name: Notification.Name("UpdateStatusBarStyle"),
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(updateNavigationBar(_:)),
            name: Notification.Name("UpdateNavigationBarStyle"),
            object: nil
        )
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        isDarkTheme ? .lightContent : .darkContent
    }

    @objc private func updateStatusBar(_ notification: Notification) {
        guard let userInfo = notification.userInfo else { return }
        if let dark = userInfo["isDarkTheme"] as? Bool {
            isDarkTheme = dark
        }
        if let red = userInfo["red"] as? Double,
           let green = userInfo["green"] as? Double,
           let blue = userInfo["blue"] as? Double,
           let alpha = userInfo["alpha"] as? Double {
            statusBarBackgroundView.backgroundColor = UIColor(
                red: red, green: green, blue: blue, alpha: alpha
            )
        }
        setNeedsStatusBarAppearanceUpdate()
    }

    @objc private func updateNavigationBar(_ notification: Notification) {
        guard let userInfo = notification.userInfo else { return }
        if let red = userInfo["red"] as? Double,
           let green = userInfo["green"] as? Double,
           let blue = userInfo["blue"] as? Double,
           let alpha = userInfo["alpha"] as? Double {
            navigationBarBackgroundView.backgroundColor = UIColor(
                red: red, green: green, blue: blue, alpha: alpha
            )
        }
    }
}
