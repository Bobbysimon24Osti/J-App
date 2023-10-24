package com.osti.juniorapp.application

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.osti.juniorapp.notifiche.JuniorNotifiche

class MyFirebaseMessagingService : FirebaseMessagingService() {

    var myToken: String? = null

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        myToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notClass = JuniorNotifiche(applicationContext)
        notClass.showNotifica()
    }
}
