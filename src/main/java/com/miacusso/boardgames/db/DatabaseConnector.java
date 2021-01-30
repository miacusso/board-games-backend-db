package com.miacusso.boardgames.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnector {

	private final Connection connection;

	public DatabaseConnector() {

		Connection conn = null;
		try {
			// Development:
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/board-games", "postgres", "postgres");
			// Production:
			//Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/id15411513_board_games", "id15411513_miacusso", "Maximiliano1acu$$o");
			System.out.println("Connection success.");
		} catch (SQLException e) {
			System.out.println("Connection failure.");
			e.printStackTrace();
		}
		this.connection = conn;
	}

	public void insertGameResult(Integer winner, Integer game) {
		GameResultDBO result = new GameResultDBO();
		result.setDate(LocalDate.now());
		result.setWinner(winner);
		result.setGame(game);
		this.insertGameResult(result);
	}

	public void insertGameResult(GameResultDBO gameResult) {
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO results(date, winner, game) VALUES (?,?,?)");
			preparedStatement.setDate(1, Date.valueOf(gameResult.getDate()));
			preparedStatement.setInt(2, gameResult.getWinner());
			preparedStatement.setInt(3, gameResult.getGame());
			System.out.println(preparedStatement);
			preparedStatement.execute();

			preparedStatement.close();

		} catch (SQLException e) {
			System.out.println("Insertion failure.");
			e.printStackTrace();
		}
	}

	public List<PlayerDBO> retrievePlayersForGame(GameDBO game) {

		List<PlayerDBO> players = new ArrayList<PlayerDBO>();

		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT * FROM players WHERE (game = ?)");
			preparedStatement.setInt(1, game.getId());
			System.out.println(preparedStatement);
			ResultSet result = preparedStatement.executeQuery();

			while (result.next()) {
				PlayerDBO player = new PlayerDBO();
				player.setId(result.getInt("id"));
				player.setName(result.getString("name"));
				player.setGame(result.getInt("game"));
				players.add(player);
			}

			preparedStatement.close();

		} catch (SQLException e) {
			System.out.println("Retrieve failure.");
			e.printStackTrace();
		}

		return players;
	}

	public Map<String, Integer> retrieveResultsCountForGame(GameDBO game) {

		Map<String, Integer> responseMap = new HashMap<String, Integer>();

		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT players.name, COUNT(winner) FROM players LEFT JOIN results ON players.id = results.winner WHERE (players.game = ?) GROUP BY players.name;");
			preparedStatement.setInt(1, game.getId());
			System.out.println(preparedStatement);
			ResultSet result = preparedStatement.executeQuery();
			while (result.next()) {
				responseMap.put(result.getString("name"), result.getInt("count"));
			}

			preparedStatement.close();

		} catch (SQLException e) {
			System.out.println("Retrieve failure.");
			e.printStackTrace();
		}
		return responseMap;
	}

	public void removeResultsForGame(GameDBO game) {
		try {

			PreparedStatement preparedStatement = this.connection.prepareStatement("DELETE FROM results WHERE (game = ?)");
			preparedStatement.setInt(1, game.getId());
			System.out.println(preparedStatement);
			preparedStatement.execute();

			preparedStatement.close();

		} catch (SQLException e) {
			System.out.println("Delete failure.");
			e.printStackTrace();
		}
	}

}
