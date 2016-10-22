package ru.dron.activevocabe.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableColumn;

/**
 * Created by Andrey on 22.10.2016.
 */
public class PTableColumn<S, T> extends TableColumn<S, T> {
    private DoubleProperty percentageWidth = new SimpleDoubleProperty();

    public PTableColumn() {
        tableViewProperty().addListener((observable, oldValue, newValue) -> {
            ReadOnlyDoubleProperty tableWidth = getTableView().widthProperty();
            this.prefWidthProperty().bind(createPercentageWidthBinding(tableWidth));
        });
    }

    private DoubleBinding createPercentageWidthBinding(ReadOnlyDoubleProperty tableWidth) {
        return Bindings.createDoubleBinding(
                () -> {
                    // If the user doesn't define the percentage
                    if (percentageWidth.get() <= 0) {
                        return getWidth();
                    } else {
                        double tableWidthDouble = tableWidth.get();
                        return percentageWidth.get() * tableWidthDouble;
                    }
                }, percentageWidth, tableWidth);
    }

    public double getPercentageWidth() {
        return percentageWidth.get();
    }

    public DoubleProperty percentageWidthProperty() {
        return percentageWidth;
    }

    public void setPercentageWidth(double value) {
        if (value >= 0 && value <= 1) {
            percentageWidth.set(value);
        } else {
            throw new IllegalArgumentException(String.format("The provided percentage width is not between 0.0 and 1.0. Value is: %1$s", value));
        }
    }
}
