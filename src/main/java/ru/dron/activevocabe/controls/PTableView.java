package ru.dron.activevocabe.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableView;

/**
 * Created by Andrey on 22.10.2016.
 */
public class PTableView<S> extends TableView<S> {
    private final DoubleProperty percentageWidth = new SimpleDoubleProperty(1);
    private final DoubleProperty percentageHeight = new SimpleDoubleProperty(1);

    public PTableView() {
        if (getScene() != null) {
            setPrefWidth(getScene().getWidth() * getPercentageWidth());
            setPrefHeight(getScene().getHeight() * getPercentageHeight());
        }
    }

    public void resize() {
        if (getScene() != null) {
            setPrefWidth(getScene().getWidth() * getPercentageWidth());
            setPrefHeight(getScene().getHeight() * getPercentageHeight());
        } else {
            System.out.println("getScene==null");
        }
    }

    public final DoubleProperty percentageWidthProperty() {
        return this.percentageWidth;
    }

    public final double getPercentageWidth() {
        return this.percentageWidthProperty().get();
    }

    public final void setPercentageWidth(double value) throws IllegalArgumentException {
        if (value >= 0 && value <= 1) {
            this.percentageWidthProperty().set(value);
        } else {
            throw new IllegalArgumentException(String.format("The provided percentage width is not between 0.0 and 1.0. Value is: %1$s", value));
        }
    }

    public final DoubleProperty percentageHeightProperty() {
        return this.percentageHeight;
    }

    public final double getPercentageHeight() {
        return this.percentageHeightProperty().get();
    }

    public final void setPercentageHeight(double value) throws IllegalArgumentException {
        if (value >= 0 && value <= 1) {
            this.percentageHeightProperty().set(value);
        } else {
            throw new IllegalArgumentException(String.format("The provided percentage width is not between 0.0 and 1.0. Value is: %1$s", value));
        }
    }
}
