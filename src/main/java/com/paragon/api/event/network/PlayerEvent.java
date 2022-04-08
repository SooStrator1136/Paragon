package com.paragon.api.event.network;

public class PlayerEvent {

    private String name;

    public PlayerEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static class PlayerJoinEvent extends PlayerEvent {
        public PlayerJoinEvent(String name) {
            super(name);
        }
    }

    public static class PlayerLeaveEvent extends PlayerEvent {
        public PlayerLeaveEvent(String name) {
            super(name);
        }
    }

}
