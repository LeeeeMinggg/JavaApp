package com.cineticket.desktop.dao;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.model.Movie;
import com.cineticket.desktop.model.MovieStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcMovieDao implements MovieDao {
    private final ConnectionFactory connectionFactory;
    public JdbcMovieDao(ConnectionFactory connectionFactory) { this.connectionFactory = connectionFactory; }

    @Override
    public List<Movie> findNowShowing(String keyword) {
        String sql = "SELECT * FROM movies WHERE status='NOW_SHOWING' AND (LOWER(title) LIKE ? OR LOWER(genre) LIKE ?) ORDER BY release_date DESC";
        String query = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, query); statement.setString(2, query);
            return readAll(statement);
        } catch (SQLException exception) { throw new DaoException("Không thể tải danh sách phim", exception); }
    }

    @Override
    public List<Movie> findAll() {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM movies ORDER BY title")) {
            return readAll(statement);
        } catch (SQLException exception) { throw new DaoException("Không thể tải danh sách phim", exception); }
    }

    @Override
    public Optional<Movie> findById(String movieId) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM movies WHERE movie_id=?")) {
            statement.setString(1, movieId);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? Optional.of(map(result)) : Optional.empty(); }
        } catch (SQLException exception) { throw new DaoException("Không thể tìm phim", exception); }
    }

    @Override public void save(Movie movie) { write(movie, false); }
    @Override public void update(Movie movie) { write(movie, true); }

    private void write(Movie movie, boolean update) {
        String sql = update
                ? "UPDATE movies SET title=?,description=?,genre=?,duration=?,release_date=?,status=?,poster_url=? WHERE movie_id=?"
                : "INSERT INTO movies(title,description,genre,duration,release_date,status,poster_url,movie_id) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection connection = connectionFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, movie.getTitle()); statement.setString(2, movie.getDescription());
            statement.setString(3, movie.getGenre()); statement.setInt(4, movie.getDuration());
            statement.setDate(5, Date.valueOf(movie.getReleaseDate())); statement.setString(6, movie.getStatus().name());
            statement.setString(7, movie.getPosterUrl()); statement.setString(8, movie.getMovieId());
            statement.executeUpdate();
        } catch (SQLException exception) { throw new DaoException("Không thể lưu phim", exception); }
    }

    private List<Movie> readAll(PreparedStatement statement) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) { while (result.next()) movies.add(map(result)); }
        return movies;
    }

    static Movie map(ResultSet result) throws SQLException {
        return new Movie(result.getString("movie_id"), result.getString("title"), result.getString("description"),
                result.getString("genre"), result.getInt("duration"), result.getDate("release_date").toLocalDate(),
                MovieStatus.valueOf(result.getString("status")), result.getString("poster_url"));
    }
}
