package com.ludolegends.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ludolegends.game.navigation.LudoNavHost
import com.ludolegends.game.ui.theme.LudoLegendsTheme
import com.ludolegends.game.vm.GameViewModel

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge();setContent{LudoLegendsTheme{val vm:GameViewModel=viewModel();LudoNavHost(vm)}}}}
