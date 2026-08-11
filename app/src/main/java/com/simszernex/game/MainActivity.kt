package com.simszernex.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.simszernex.game.model.PowerType
import com.simszernex.game.ui.GameViewModel
import com.simszernex.game.ui.screens.GameScreen
import com.simszernex.game.ui.theme.SimsZernexTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimsZernexTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.state.collectAsState()
                    GameScreen(
                        state = state,
                        onCreateCharacter = viewModel::createCharacter,
                        onChangeRoom = viewModel::changeRoom,
                        onPerformAction = viewModel::performAction,
                        onUsePower = viewModel::usePower,
                        onLearnPower = viewModel::learnPower,
                        onUnlockAllPowers = viewModel::unlockAllPowers,
                        onBuyFood = viewModel::buyFood,
                        onChangeTab = viewModel::changeTab,
                        onTalkToNpc = viewModel::talkToNpc,
                        onProposeMarriage = viewModel::proposeMarriage,
                        onTryForChild = viewModel::tryForChild,
                        onClaimMission = viewModel::claimMission,
                        onWork = viewModel::work,
                        onTravelTo = viewModel::travelTo,
                        onJoinCareer = viewModel::joinCareer,
                        onSpecialAction = viewModel::specialAction,
                        onBuyHouse = viewModel::buyHouse,
                        onBuyItem = viewModel::buyItem,
                        onAttendAcademy = viewModel::attendAcademy
                    )
                }
            }
        }
    }
}
