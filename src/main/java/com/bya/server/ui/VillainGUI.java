package com.bya.server.ui;

import com.bya.server.game.GameState;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class VillainGUI extends JFrame {

    public VillainGUI() {
        setTitle("Villain View");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        setVisible(true);
    }

    private class GamePanel extends JPanel {
        private final double VILLAIN_HEIGHT_RATIO = 0.6;
        private final Color SHOCKWAVE_COLOR = new Color(255, 0, 100);

        private BufferedImage idleImage, attackImage1, attackImage2;
        private BufferedImage currentImage;
        private double imageAspectRatio;

        private boolean isAttacking = false;
        private Timer attackAnimationTimer;
        private Timer shockwaveTimer;

        private int shockwaveDistance = 0;
        private final int shockwaveSpeed = 25;
        private final int stripeWidth = 10;

        public GamePanel() {
            setBackground(Color.DARK_GRAY);
            loadImages();
            currentImage = idleImage;

            setFocusable(true);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE && !isAttacking) {
                        performAttack();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
            });
        }

        private void loadImages() {
            try {
                idleImage = ImageIO.read(getClass().getResource("/skin1.png"));
                attackImage1 = ImageIO.read(getClass().getResource("/skin2.png"));
                attackImage2 = ImageIO.read(getClass().getResource("/skin3.png"));

                if (idleImage != null) {
                    imageAspectRatio = (double) idleImage.getWidth() / idleImage.getHeight();
                } else {
                    imageAspectRatio = 1.0;
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        private void performAttack() {
            GameState.getInstance().processVillainAttack();

            isAttacking = true;
            currentImage = attackImage1;
            shockwaveDistance = 0;

            attackAnimationTimer = new Timer(300, e -> {
                currentImage = attackImage2;
                repaint();

                Timer returnToIdleTimer = new Timer(200, e2 -> {
                    currentImage = idleImage;
                    isAttacking = false;
                    repaint();
                });
                returnToIdleTimer.setRepeats(false);
                returnToIdleTimer.start();
            });
            attackAnimationTimer.setRepeats(false);
            attackAnimationTimer.start();

            shockwaveTimer = new Timer(16, e -> {
                shockwaveDistance += shockwaveSpeed;
                if (shockwaveDistance > getWidth()) {
                    GameState.getInstance().triggerShockwaveImpact();
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            shockwaveTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            int villainHeight = (int) (getHeight() * VILLAIN_HEIGHT_RATIO);
            int villainWidth = (int) (villainHeight * imageAspectRatio);
            int imgX = centerX - villainWidth / 2;

            if (currentImage != null) {
                int imgY = centerY - villainHeight / 2;
                g.drawImage(currentImage, imgX, imgY, villainWidth, villainHeight, this);
            }

            if (isAttacking && shockwaveTimer != null && shockwaveTimer.isRunning()) {
                g.setColor(SHOCKWAVE_COLOR);

                int leftStripeOrigin = imgX;
                int rightStripeOrigin = imgX + villainWidth;

                int leftStripeX = leftStripeOrigin - shockwaveDistance;
                int rightStripeX = rightStripeOrigin + shockwaveDistance;

                g.fillRect(leftStripeX, 0, stripeWidth, getHeight());
                g.fillRect(rightStripeX, 0, stripeWidth, getHeight());
            }
        }
    }
}