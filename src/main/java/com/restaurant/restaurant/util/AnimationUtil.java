package com.restaurant.restaurant.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class AnimationUtil {

    private AnimationUtil() {}

    /* =====================================
       BUTTON HOVER SCALE
       ===================================== */

    public static void addButtonHover(Button button) {

        ScaleTransition scaleUp =
                new ScaleTransition(Duration.millis(150), button);

        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);

        ScaleTransition scaleDown =
                new ScaleTransition(Duration.millis(150), button);

        scaleDown.setToX(1);
        scaleDown.setToY(1);

        button.setOnMouseEntered(e -> scaleUp.playFromStart());

        button.setOnMouseExited(e -> scaleDown.playFromStart());
    }

    /* =====================================
       CARD HOVER
       ===================================== */

    public static void addCardHover(Node card) {

        ScaleTransition scaleUp =
                new ScaleTransition(Duration.millis(150), card);

        scaleUp.setToX(1.03);
        scaleUp.setToY(1.03);

        ScaleTransition scaleDown =
                new ScaleTransition(Duration.millis(150), card);

        scaleDown.setToX(1);
        scaleDown.setToY(1);

        card.setOnMouseEntered(e -> scaleUp.playFromStart());

        card.setOnMouseExited(e -> scaleDown.playFromStart());
    }

    /* =====================================
       FADE IN
       ===================================== */

    public static void fadeIn(Node node) {

        node.setOpacity(0);

        FadeTransition fade =
                new FadeTransition(Duration.millis(500), node);

        fade.setFromValue(0);
        fade.setToValue(1);

        fade.play();
    }

    /* =====================================
       SLIDE FROM RIGHT
       ===================================== */

    public static void slideIn(Node node) {

        node.setTranslateX(50);

        TranslateTransition slide =
                new TranslateTransition(Duration.millis(400), node);

        slide.setFromX(50);
        slide.setToX(0);

        slide.play();
    }

    /* =====================================
       POP EFFECT
       ===================================== */

    public static void pop(Node node) {

        ScaleTransition scale =
                new ScaleTransition(Duration.millis(250), node);

        scale.setFromX(0.8);
        scale.setFromY(0.8);

        scale.setToX(1);
        scale.setToY(1);

        scale.play();
    }

    /* =====================================
       PULSE EFFECT
       ===================================== */

    public static void pulse(Node node) {

        ScaleTransition pulse =
                new ScaleTransition(Duration.millis(800), node);

        pulse.setFromX(1);
        pulse.setFromY(1);

        pulse.setToX(1.05);
        pulse.setToY(1.05);

        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);

        pulse.play();
    }
}