package dev.vankka.jdacommands.model.command;

import org.jetbrains.annotations.NotNull;

public interface CommandResult {

    class Error implements CommandResult {

        private final Exception exception;

        public Error(@NotNull Exception exception) {
            this.exception = exception;
        }

        @NotNull
        public Exception getException() {
            return exception;
        }
    }

    class Message implements CommandResult {

        private final String message;

        public Message(@NotNull String message) {
            this.message = message;
        }

        @NotNull
        public String getMessage() {
            return message;
        }
    }

    enum Generic implements CommandResult {
        SUCCESS_HANDLED,
        SUCCESS_CHECK_MARK
    }
}
