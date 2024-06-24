package screens.gamelistscreen

import com.bojan.gamelistmanager.gamelistprovider.domain.interfaces.GameListRepository
import commonui.textlist.SelectableListViewModel
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import screens.gamelistscreen.data.GameInfoUiModel
import screens.gamelistscreen.data.GameListScreenUiModel
import screens.gamelistscreen.data.GameSystemUiModel
import screens.gamelistscreen.mappers.toGameSystemUiModel

/**
 * Viewmodel used to control GameList screen.
 *
 * @param dataSource Data source of the game list.
 * @param selectableGameListViewModel ViewModel for the selectable game list.
 * @param onExport The callback invoked when export screen should be opened.
 */
class GameListScreenViewModel(
    private val dataSource: GameListRepository,
    private val onExport: () -> Unit,
    val selectableGameListViewModel: SelectableListViewModel<GameInfoUiModel> = SelectableListViewModel(),
) : ViewModel() {

    private val _uiModel = MutableStateFlow(GameListScreenUiModel(emptyList(), emptyList()))
    val uiModel = _uiModel.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.gameList.collect { gameList ->
                val sorted = gameList.sortedBy { it.system.name }
                _uiModel.value = GameListScreenUiModel(
                    gameSystems = sorted.map { it.toGameSystemUiModel() },
                    gameSystemDisplayList = sorted.map { it.toGameSystemUiModel().text },
                    selectedSystem = if (sorted.isNotEmpty()) 0 else -1,
                    selectedGame = 0,
                    searchQuery = ""
                )
                showFullGameList()
                if (sorted.isNotEmpty()) {
                    systemSelected(0)
                    gameSelected(0)
                }
            }
        }

        viewModelScope.launch {
            selectableGameListViewModel.uiModel.collect {
                gameSelected(it.selectedItem)
            }
        }
    }

    fun systemSelected(selectedIndex: Int) {
        val uiModel = _uiModel.value
        val systemList = uiModel.gameSystems
        val selectedSystem = uiModel.selectedSystem
        val selectedGame = if (systemList.isNotEmpty()) 0 else -1
        val activeSystem = if (selectedSystem >= 0 && systemList.size > selectedSystem) {
            systemList[selectedSystem]
        } else {
            GameSystemUiModel.empty
        }
        _uiModel.value = _uiModel.value.copy(
            selectedSystem = selectedIndex,
            selectedGame = selectedGame,
            searchQuery = "",
            activeSystemInfo = activeSystem
        )
        gameSelected(0)
        showFullGameList()
    }

    private fun gameSelected(selectedIndex: Int) {
        val uiModel = _uiModel.value
        val selectedGame = uiModel.selectedGame
        val games = selectableGameListViewModel.uiModel.value.items

        if (games.isNotEmpty() && games.size > selectedGame - 1) {
            _uiModel.value = _uiModel.value.copy(selectedGame = selectedIndex, activeGameInfo = games[selectedIndex])
            return

        }
        _uiModel.value = _uiModel.value.copy(selectedGame = selectedIndex, activeGameInfo = GameListScreenUiModel.emptyGameInfo)
    }

    private fun showFullGameList() {
        val uiModel = _uiModel.value
        val systemList = uiModel.gameSystems
        val selectedSystem = uiModel.selectedSystem
        if (systemList.isNotEmpty() && selectedSystem > -1) {
            val currentGames = systemList[selectedSystem].games
            selectableGameListViewModel.setItems(currentGames)
        } else {
            selectableGameListViewModel.setItems(emptyList())
        }
    }

    fun switchToExportScreen() {
        onExport()
    }
}