package com.ludolegends.game.navigation

import androidx.compose.runtime.*
import com.ludolegends.game.ui.screens.*
import com.ludolegends.game.vm.GameViewModel

private enum class Route{SPLASH,LOBBY,GAME}
@Composable fun LudoNavHost(vm:GameViewModel){var route by remember{mutableStateOf(Route.SPLASH)};val balance by vm.balance.collectAsState();when(route){Route.SPLASH->SplashScreen{route=Route.LOBBY};Route.LOBBY->LobbyScreen(balance){vm.startMatch(it);route=Route.GAME};Route.GAME->GameScreen(vm){route=Route.LOBBY}}}
