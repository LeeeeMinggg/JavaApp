package com.cineticket.desktop.model;

public final class Staff extends User {
    private final String staffId;

    public Staff(String userId, String username, String passwordHash,
                 String fullName, String email, boolean active) {
        super(userId, username, passwordHash, fullName, email, active);
        this.staffId = userId;
    }

    public void manageMovies(Movie movie, String title, String genre) {
        movie.updateMovie(title, movie.getDescription(), genre, movie.getDuration(),
                movie.getReleaseDate(), movie.getStatus(), movie.getPosterUrl());
    }

    public void manageRooms(Room room, String roomName, String status) { room.updateRoom(roomName, status); }
    public void manageShowtimes(Showtime showtime, ShowtimeStatus status) { showtime.updateShowtime(showtime.getStartTime(), showtime.getEndTime(), status); }
    public void manageSeats(Seat seat) { seat.getDetails(); }
    public void manageTickets(Ticket ticket) { ticket.validate(); }
    public String getStaffId() { return staffId; }
    @Override public UserRole getRole() { return UserRole.STAFF; }
}
