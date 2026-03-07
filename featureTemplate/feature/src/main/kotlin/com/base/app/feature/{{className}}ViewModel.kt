package {{packageName}}.{{featureName}}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import {{packageName}}.model.error.MessageType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class {{className}}ViewModel @Inject constructor(

) : ViewModel() {

    private val _{{lowerCamelClassName}}State: MutableStateFlow<{{className}}State> = MutableStateFlow({{className}}State.Loading)
    val {{lowerCamelClassName}}State: StateFlow<{{className}}State> get() = _{{lowerCamelClassName}}State

    private val _{{lowerCamelClassName}}ModalEffect = MutableStateFlow<{{className}}ModalEffect>({{className}}ModalEffect.Idle)
    val {{lowerCamelClassName}}ModalEffect: StateFlow<{{className}}ModalEffect> get() = _{{lowerCamelClassName}}ModalEffect

    private val _{{lowerCamelClassName}}Effect = MutableSharedFlow<{{className}}Effect>()
    val {{lowerCamelClassName}}Effect: SharedFlow<{{className}}Effect> get() = _{{lowerCamelClassName}}Effect.asSharedFlow()

    fun dismissDialog() {
        _{{lowerCamelClassName}}ModalEffect.update { {{className}}ModalEffect.Idle }
    }

    private fun showSnackBar(messageType: MessageType) {
        viewModelScope.launch {
            _{{lowerCamelClassName}}Effect.emit({{className}}Effect.ShowSnackBar(messageType))
        }
    }
}
