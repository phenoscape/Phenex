package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.bbop.framework.AbstractGUIComponent;
import org.phenoscape.view.LogViewComponentFactory.LogViewAppender;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.SortDisabler;
import phenote.util.EverythingEqualComparator;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class LogViewComponent extends AbstractGUIComponent {
  
  private final LogViewAppender appender;

  public LogViewComponent(String id, LogViewAppender appender) {
    super(id);
    this.appender = appender;
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final SortedList<LoggingEvent> sortedMessages = new SortedList<LoggingEvent>(this.appender.getMessages(), new EverythingEqualComparator<LoggingEvent>());
    final EventTableModel<LoggingEvent> tableModel = new EventTableModel<LoggingEvent>(sortedMessages, new LogTableFormat());
    final JTable logTable = new BugWorkaroundTable(tableModel);
    logTable.setDefaultRenderer(Level.class, new LevelRenderer());
    final TableComparatorChooser<LoggingEvent> sortChooser = new TableComparatorChooser<LoggingEvent>(logTable, sortedMessages, false);
    sortChooser.addSortActionListener(new SortDisabler());
    logTable.putClientProperty("Quaqua.Table.style", "striped");
    logTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    logTable.getColumnModel().getColumn(0).setWidth(15);
    logTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
    this.add(new JScrollPane(logTable), BorderLayout.CENTER);
  }
  
  private class LevelComparator implements Comparator<Level> {

    public int compare(Level o1, Level o2) {
      if (o1.equals(o2)) { return 0; }
      if (o1.isGreaterOrEqual(o2)) {
        return 1;
      } else {
        return -1;
      }
    }
    
  }
  
  private class LevelRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final Level level = (Level)value;
      if (level.equals(Level.DEBUG)) {
        this.setBackground(Color.GREEN);
      } else if (level.equals(Level.INFO)) {
        this.setBackground(Color.BLUE);
      } else if (level.equals(Level.WARN)) {
        this.setBackground(Color.YELLOW);
      } else if (level.isGreaterOrEqual(Level.ERROR)) {
        this.setBackground(Color.RED);
      }
      return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
    }
    
  }
  
  private class LogTableFormat implements AdvancedTableFormat<LoggingEvent> {

    public int getColumnCount() {
      return 4;
    }

    public String getColumnName(int column) {
      switch(column) {
      case(0): return "";
      case(1): return "Time";
      case(2): return "Source";
      case(3): return "Message";
      default: return null;
      }
    }

    public Object getColumnValue(LoggingEvent event, int column) {
      switch(column) {
      case(0): return event.getLevel();
      case(1): return new Date(event.timeStamp);
      case(2): return event.getLoggerName();
      case(3): return event.getMessage().toString();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch(column) {
      case(0): return Level.class;
      case(1): return Date.class;
      case(2): return String.class;
      case(3): return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch(column) {
      case(0): return new LevelComparator();
      case(1): return GlazedLists.comparableComparator();
      case(2): return Strings.getNaturalComparator();
      case(3): return Strings.getNaturalComparator();
      default: return null;
      }
    }

  }

}
