package org.phenoscape.view;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.bbop.framework.AbstractGUIComponent;
import org.phenoscape.app.PrefObj;
import org.phenoscape.model.TermField;

import phenote.util.Collections;

public class OntologyPreferencesComponent extends AbstractGUIComponent {

  public OntologyPreferencesComponent(String id) {
    super(id);
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final JTabbedPane tabPane = new JTabbedPane();
    for (TermField field : TermField.values()) {
      tabPane.add(this.createOntologyPrefPanel(field));
    }
    this.add(tabPane, BorderLayout.CENTER);
  }
  
  private JComponent createOntologyPrefPanel(TermField field) {
    final JPanel prefPanel = new JPanel(new BorderLayout());
    prefPanel.setName(field.displayName());
    final JTable table = new JTable(new OntologyPrefTableModel(field));
    table.putClientProperty("Quaqua.Table.style", "striped");
    prefPanel.add(new JScrollPane(table), BorderLayout.CENTER);
    return prefPanel;
  }
  
  private class OntologyPrefTableModel extends AbstractTableModel {

    private final TermField field;

    OntologyPrefTableModel(TermField field) {
      this.field = field;
    }

    public int getColumnCount() {
      return 1;
    }

    public int getRowCount() {
      return this.getURLs().size() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      final List<String> urls = this.getURLs();
      if (rowIndex > (urls.size() - 1)) {
        return "";
      } else {
        return urls.get(rowIndex);
      }
    }

    @Override
    public String getColumnName(int column) {
      return "Ontology URLs";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
      final List<String> urls = this.getURLs();
      final boolean delete = (value == null) || (value.equals(""));
      if (rowIndex > (urls.size() - 1)) {
        if (!delete) {
          urls.add(value.toString());
        }
      } else {
        if (!delete) {
          urls.set(rowIndex, value.toString());
        } else {
          urls.remove(rowIndex);
        }
      }
      this.writeURLs(urls);
      this.fireTableDataChanged();
    }

    private List<String> getURLs() {
      final String urlString = this.getPrefsRoot().get(this.field.name(), this.field.defaultURLs());
      final List<String> urls = new ArrayList<String>(Arrays.asList(urlString.split(" ")));
      urls.remove(""); // this is necessary because an empty list will be returned as a list with a single ""
      return urls;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> getOntologyPrefs() {
      try {
        final Object obj = PrefObj.getObject(this.getPrefsRoot(), this.field.name());
        if (obj != null) {
          return (Map<String, String>)obj;
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (BackingStoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return new HashMap<String, String>();
    }

    private void writeURLs(List<String> urls) {
      final String urlString = Collections.join(urls, " ");
      this.getPrefsRoot().put(this.field.name(), urlString);
    }
    
    private void writeOntologyPrefs(Map<String, String> prefs) {
      try {
        PrefObj.putObject(this.getPrefsRoot(), this.field.name(), prefs);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (BackingStoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    private Preferences getPrefsRoot() {
      return Preferences.userNodeForPackage(this.getClass()).node("ontologyConfig");
    }

  }

}
