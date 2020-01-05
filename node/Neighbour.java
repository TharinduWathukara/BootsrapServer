/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package node;

/**
 *
 * @author Tharindu Wathukara
 */
class Neighbour {
	private String ip;
	private int port;
	private String username;

	public Neighbour(String ip, int port, String username) {
		this.ip = ip;
		this.port = port;
		this.username = username;
	}

	public String getIp() {
		return this.ip;
	}

	public String getUsername() {
		return this.username;
	}

	public int getPort() {
		return this.port;
	}
}
