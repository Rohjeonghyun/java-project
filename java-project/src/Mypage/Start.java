package Mypage;

import javax.swing.SwingUtilities;

import Mypage.MyPageFrame;

public class Start {
	// Main.java
	/**
	 * 프로그램 시작 클래스
	 *  - MyPageFrame(마이페이지) 창을 가장 먼저 띄운다.
	 */
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            MyPageFrame frame = new MyPageFrame();
	            frame.setVisible(true);
	        });
	    }
	}
