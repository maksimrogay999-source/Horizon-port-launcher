package hnm.horizon.launcher;

public abstract class MainUiEvent {
    public static class ShowToast extends MainUiEvent {
        public final String message;
        public ShowToast(String message) { this.message = message; }
    }
    public static class CopyToClipboard extends MainUiEvent {
        public final String text;
        public CopyToClipboard(String text) { this.text = text; }
    }
    public static class LaunchGame extends MainUiEvent {}
    public static class NavigateToSetup extends MainUiEvent {}
}