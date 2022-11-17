package com.vladtop.game_task

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

@SuppressLint("CustomSplashScreen")
class SplashFragment : Fragment(R.layout.fragment_splash), RemoteConfigRepository.OnResultListener {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val remoteConfigRepository = RemoteConfigRepository(this)
        remoteConfigRepository.init()
    }

    override fun onSuccess(gamePass: Boolean, webLink: String) {
        if (gamePass) navigateToGame()
        else navigateToWeb(webLink)
    }

    private fun navigateToWeb(webLink: String) {
        val bundle = bundleOf(WEB_LINK to webLink)
        findNavController().navigate(R.id.action_splashFragment_to_webFragment, bundle)
    }

    private fun navigateToGame() {
        findNavController().navigate(R.id.action_splashFragment_to_gameMenuFragment)
    }

    override fun onError(message: String) {
        showOfflineModeMessage(message)
        offlineMode()
    }

    private fun offlineMode() {
        navigateToGame()
    }

    private fun showOfflineModeMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}