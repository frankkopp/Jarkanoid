/*
 * MIT License
 *
 * Copyright (c) 2018 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fko.jarkanoid.view;

import fko.jarkanoid.controller.MainController;
import fko.jarkanoid.model.GameModel;
import fko.jarkanoid.model.HighScore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class HighScoreListView {

  private static final Logger LOG = LoggerFactory.getLogger(HighScoreListView.class);

  private final TableView<TableRowBean> highScoreTable;
  private final GameModel model;
  private final MainController controller;
  private final ObservableList<TableRowBean> observableList;

  private HighScore.HighScoreEntry newestEntry;

  /**
   * TODO: fix warnings
   *
   * @param model the model
   * @param controller the controller
   * @param highScoreTable the model's high score table
   */
  HighScoreListView(
          final GameModel model,
          final MainController controller,
          final TableView<TableRowBean> highScoreTable) {

    LOG.debug("Building HighScoreListView");

    this.model = model;
    this.controller = controller;
    this.highScoreTable = highScoreTable;
    this.observableList = FXCollections.observableArrayList(new ArrayList<TableRowBean>());
    this.newestEntry = null;

    // clear any columns which have been set by FXML
    highScoreTable.getColumns().clear();

    double tablePrefWidth = highScoreTable.getPrefWidth();

    // add new columns
    TableColumn<TableRowBean, String> placeCol =
        createTableColumn("Place", 0.1 * tablePrefWidth, TableRowBean::placeProperty);
    TableColumn<TableRowBean, String> nameCol =
        createTableColumn("Name", 0.4 * tablePrefWidth, TableRowBean::nameProperty);
    TableColumn<TableRowBean, String> scoreCol =
        createTableColumn("Score", 0.15 * tablePrefWidth, TableRowBean::scoreProperty);
    TableColumn<TableRowBean, String> levelCol =
        createTableColumn("Level", 0.1 * tablePrefWidth, TableRowBean::levelProperty);
    TableColumn<TableRowBean, String> dateCol =
        createTableColumn("Date", 0.25 * tablePrefWidth, TableRowBean::dateProperty);
    highScoreTable.getColumns().addAll(placeCol, nameCol, scoreCol, levelCol, dateCol);

    this.highScoreTable.setItems(observableList);

    highScoreTable.setEditable(false);
    highScoreTable.setMouseTransparent(false);

    updateList();
  }

  private TableColumn<TableRowBean, String> createTableColumn(
          final String columnTitle,
          final double width,
          final Function<TableRowBean, ObservableValue<String>> property) {

    TableColumn<TableRowBean, String> column = new TableColumn<>(columnTitle);
    column.setResizable(false);
    column.setSortable(false);
    column.setEditable(true);
    column.setPrefWidth(width);
    column.setMinWidth(width);
    column.setMaxWidth(width);

    //    column.setCellValueFactory(new PropertyValueFactory<>(property));
    column.setCellValueFactory(cellData -> property.apply(cellData.getValue()));

    try {
      column.impl_setReorderable(false);
      // In Java 9 the above codes would break because of removal of impl_.
      // Despite these changes, it introduces convenient public methods that you can use which are:
      // setReorderable(boolean value)
      // getReorderable()
      // for TableColumnBase such as TableColumn to be used for set Reorderable,
    } catch (NoSuchMethodError ignore) { // happens in Java9
      LOG.info("impl_setReorderable not available - probably Java9?");
    }

    return column;
  }

  /**
   * Called by the controller whenever the model's highscore list shall be updated
   * @param newEntry
   */
  public void updateList(final HighScore.HighScoreEntry newEntry) {
    newestEntry = newEntry;
    updateList();
  }

  private void updateList() {
    LOG.debug("Update HighScoreListView from model");
    observableList.clear();

    final AtomicInteger counter = new AtomicInteger(1);
    model
            .getHighScoreManager()
            .stream()
            .limit(15)
            .forEach(
                    entry -> observableList.add(
                            new TableRowBean(
                                    Integer.toString(counter.getAndIncrement()),
                                    entry.name,
                                    entry.score,
                                    entry.level,
                                    entry.date)));

    if (LOG.isDebugEnabled()) {
      LOG.debug("{}", observableList);
    }
  }

  /** Represents on row of data to display in the high score table */
  public class TableRowBean {

    private final StringProperty place;
    private final StringProperty name;
    private final StringProperty score;
    private final StringProperty level;
    private final StringProperty date;

    TableRowBean(
        final String place,
        final String name,
        final int score,
        final int level,
        final LocalDateTime date) {

      this.place = new SimpleStringProperty(place);
      this.name = new SimpleStringProperty(name);
      this.score = new SimpleStringProperty(String.format("%,d", score));
      this.level = new SimpleStringProperty(Integer.toString(level));
      this.date =
          new SimpleStringProperty(
              date.format(
                  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)));
    }

    StringProperty placeProperty() {
      return place;
    }

    String getName() {
      return name.get();
    }

    StringProperty nameProperty() {
      return name;
    }

    void setPlace(final String place) {
      this.place.set(place);
    }

    String getPlace() {
      return place.get();
    }

    String getScore() {
      return score.get();
    }

    StringProperty scoreProperty() {
      return score;
    }

    String getLevel() {
      return level.get();
    }

    StringProperty levelProperty() {
      return level;
    }

    String getDate() {
      return date.get();
    }

    StringProperty dateProperty() {
      return date;
    }

    @Override
    public String toString() {

      return "TableRowBean{"
          + "name="
          + name
          + ", score="
          + score
          + ", level="
          + level
          + ", date="
          + date
          + '}';
    }
  }
}
