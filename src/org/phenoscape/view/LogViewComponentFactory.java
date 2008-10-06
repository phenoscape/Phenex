package org.phenoscape.view;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.bbop.framework.AbstractComponentFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

public class LogViewComponentFactory extends AbstractComponentFactory<LogViewComponent> {
  
  private final LogViewAppender appender = new LogViewAppender();

  public LogViewComponentFactory() {
    super();
    this.appender.setThreshold(Level.INFO);
    Logger.getRootLogger().addAppender(this.appender);
  }

  @Override
  public LogViewComponent doCreateComponent(String id) {
    return new LogViewComponent(id, this.appender);
  }

  public FactoryCategory getCategory() {
    return FactoryCategory.INFO;
  }

  public String getID() {
    return "log_view_component";
  }

  public String getName() {
    return "Error Log";
  }
  
  @Override
  public boolean isSingleton() {
    return true;
  }
  
  /**
   * This appender just collects all received log messages into an EventList.
   * @author jim
   */
  public static class LogViewAppender extends AppenderSkeleton {
    
    private final EventList<LoggingEvent> messages = new BasicEventList<LoggingEvent>();

    @Override
    protected void append(LoggingEvent event) {
      this.messages.add(event);
    }
    
    public EventList<LoggingEvent> getMessages() {
      return this.messages;
    }

    @Override
    public void close() {}

    @Override
    public boolean requiresLayout() {
      return false;
    }
    
  }

}
