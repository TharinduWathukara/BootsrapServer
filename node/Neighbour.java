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

	public Neighbour(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}
}
