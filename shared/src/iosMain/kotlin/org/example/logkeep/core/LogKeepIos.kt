package org.example.logkeep.core

import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification

object LogKeepIos {

    fun start(config: LogKeepConfig = LogKeepConfig()) {
        LogKeep.init(config)
        if (!config.isEnabled) return

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillResignActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { LogKeep.markSessionClean() }

        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { LogKeep.markSessionActive() }
    }
}
