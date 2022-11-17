package com.vladtop.game_task.presentation.SplashScreen

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vladtop.game_task.R
import com.vladtop.game_task.data.RemoteConfigRepository
import com.vladtop.game_task.data.WEB_LINK_KEY


class SplashFragment : Fragment(R.layout.fragment_splash), RemoteConfigRepository.OnResultListener {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRemoteConfig()
        onBackPressed()
    }

    private fun getRemoteConfig() {
        val remoteConfigRepository = RemoteConfigRepository(this)
        remoteConfigRepository.init()
    }

    override fun onSuccess(gamePass: Boolean, webLink: String) {
        if (gamePass) navigateToGame()
        else navigateToWeb(webLink)
    }

    private fun navigateToWeb(webLink: String) {
        val bundle = bundleOf(WEB_LINK_KEY to webLink)
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

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showBackPressedMessage()
                }
            })
    }

    private fun showBackPressedMessage() {
        Toast.makeText(requireContext(), "Back Pressed", Toast.LENGTH_SHORT).show()
    }


}