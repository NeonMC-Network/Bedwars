package me.naptie.bukkit.game.bedwars.tools;

import java.sql.*;
import java.util.*;

public class MySQL {

	private Connection connection;

	private String username, password;
	private String address, database, timezone;
	private boolean useSSL;

	private String table;

	public MySQL(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public MySQL(String username, String password, String address, String database, String table, String timezone, boolean useSSL) {
		this.username = username;
		this.password = password;
		this.address = address;
		this.database = database;
		this.table = table;
		this.timezone = timezone;
		this.useSSL = useSSL;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public Editor connect() throws SQLException {
		this.connection = DriverManager.getConnection("jdbc:mysql://" + this.address + "/" + this.database + "?useSSL=" + this.useSSL + "&serverTimezone=" + this.timezone, this.username, this.password);
		Statement statement = this.connection.createStatement();
		return new Editor(statement, this.table);
	}

	public void disconnect() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Editor reconnect() throws SQLException {
		this.disconnect();
		return this.connect();
	}

	public class Editor {

		private Statement statement;
		private String table;

		Editor(Statement statement, String table) {
			this.statement = statement;
			this.table = table;
			try {
				this.statement.executeUpdate("create table if not exists " + this.table + " ( `id` INT NOT NULL , `type` TEXT NOT NULL , `server` TEXT NOT NULL , `name` TEXT NOT NULL , `min` INT NOT NULL , `max` INT NOT NULL , `players` INT NOT NULL , `spectators` INT NOT NULL , `state` TEXT NOT NULL , `perteam` INT NOT NULL , PRIMARY KEY ( `id` ) ) charset=utf8;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public MySQL getMySQL() {
			return MySQL.this;
		}

		public boolean set(int id, String key, Object object) {
			int result;

			if (object == null) {
				try {
					this.statement.executeUpdate("delete from " + this.table + " where id = '" + id + "';");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				result = this.statement.executeUpdate("update " + this.table + " set " + key + " = '" + object + "' where id ='" + id + "'");
			} catch (SQLException e) {
				return false;
			}

			if (result == 0) {
				return false;
			}

			return false;
		}

		public boolean set(int id, String type, String server, String name, int min, int max, int players, int spectators, String state, int perteam) {
			int result;

			if (type == null || server == null || name == null || state == null) {
				try {
					this.statement.executeUpdate("delete from " + this.table + " where id = '" + id + "';");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				result = this.statement.executeUpdate("update " + this.table + " set type = '" + type + "', server = '" + server + "', name = '" + name + "', min = '" + min + "', max = '" + max + "', players = '" + players + "', spectators = '" + spectators + "', state = '" + state + "', perteam = '" + perteam + "' where id ='" + id + "'");
			} catch (SQLException e) {
				return false;
			}

			if (result == 0) {
				try {
					result = this.statement.executeUpdate("insert into " + this.table + " values ('" + id + "', '" + type + "', '" + server + "', '" + name + "', '" + min + "', '" + max + "', '" + players + "', '" + spectators + "', '" + state + "', '" + perteam + "');");
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
				return result == 1;
			}

			return false;
		}

		public Map<String, Object> get(int key) {
			ResultSet resultSet;
			try {
				resultSet = this.statement.executeQuery("select * from " + this.table + ";");
				while (resultSet.next()) {
					if (resultSet.getInt("id") == key) {
						String type = resultSet.getString("type");
						String server = resultSet.getString("server");
						String name = resultSet.getString("name");
						int min = resultSet.getInt("min");
						int max = resultSet.getInt("max");
						int players = resultSet.getInt("players");
						int spectators = resultSet.getInt("spectators");
						String state = resultSet.getString("state");
						int perteam = resultSet.getInt("perteam");
						Map<String, Object> result = new HashMap<>();
						result.put("id", key);
						result.put("type", type);
						result.put("server", server);
						result.put("name", name);
						result.put("min", min);
						result.put("max", max);
						result.put("players", players);
						result.put("spectators", spectators);
						result.put("state", state);
						result.put("perteam", perteam);
						return result;
					}
				}
			} catch (SQLException ignored) {
			}
			return null;
		}

		public boolean contains(int key) {
			ResultSet resultSet;
			try {
				resultSet = this.statement.executeQuery("select * from " + this.table + ";");
				while (resultSet.next()) {
					if (resultSet.getInt("id") == key) {
						return true;
					}
				}
			} catch (SQLException ignored) {
			}
			return false;
		}

		public boolean contains(String key, Object obj) {
			ResultSet resultSet;
			try {
				resultSet = this.statement.executeQuery("select * from " + this.table + ";");
				while (resultSet.next()) {
					if (Objects.equals(resultSet.getString(key), obj)) {
						return true;
					}
				}
			} catch (SQLException ignored) {
			}
			return false;
		}

		public List<Integer> getAllKeys() {
			try {
				ResultSet resultSet = this.statement.executeQuery("select id from " + this.table + ";");
				List<Integer> list = new ArrayList<>();
				while (resultSet.next()) {
					list.add(resultSet.getInt(1));
				}
				return list;
			} catch (SQLException ignored) {
			}
			return null;
		}

	}

}
