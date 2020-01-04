package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.MatrixCell;
import org.phenoscape.model.MatrixCellSelectionListener;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.State;

public class StateSupportInKBComponent extends PhenoscapeGUIComponent {

	private JPanel panel;
	private CellSelectionListener cellListener = new CellSelectionListener();
	private String absenceLink = "https://kb.phenoscape.org/#/facet?tab=taxonannotations&entity=%%ENTITYURI%%&taxon=%%TAXONURI%%&quality=ps:inferred_absence";
	private String presenceLink = "https://kb.phenoscape.org/#/facet?tab=taxonannotations&entity=%%ENTITYURI%%&taxon=%%TAXONURI%%&quality=ps:inferred_presence";

	public StateSupportInKBComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
		this.getController().addMatrixCellSelectionListener(this.cellListener);
	}

	@Override
	public void cleanup() {
		this.getController().removeMatrixCellSelectionListener(cellListener);
		super.cleanup();
	}

	private void initializeInterface() {
		this.setLayout(new BorderLayout());
		this.panel = new JPanel();
		this.add(this.panel, BorderLayout.CENTER);
	}

	private void open(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				log().error("Failed to open URL", e);
			}
		} 
	}

	private void clearInterface() {
		log().debug("Clearing panel");
		this.panel.removeAll();
		this.panel.validate();
	}

	private void displaySupportForCell(MatrixCell cell) {
		String taxonURL = "";
		if (cell.getTaxon().getValidName() != null) {
			taxonURL = cell.getTaxon().getValidName().getID();
		}
		String entityURL = "";
		if (cell.getCharacter().getDenotes() != null) {
			entityURL = cell.getCharacter().getDenotes().toString().replaceAll("http://purl.obolibrary.org/obo/", "").replaceAll("_", ":");
		} 
		for (State state : this.stateValuesForCell(cell)) {
			if (state.getLabel().equals("present") || state.getLabel().equals("absent")) {
				final JButton button = new JButton();
				final String kbURL;
				if (state.getLabel().equals("present")) {
					kbURL = presenceLink.replaceFirst("%%TAXONURI%%", taxonURL).replaceFirst("%%ENTITYURI%%", entityURL);
					button.setText("<HTML><P style=\"text-align: center\"><FONT color=\"#000099\"><U>Open browser to view states supporting <BR> <B>presence</B> of <BR><B>" + 
							cell.getCharacter().getLabel() + 
							"</B> in <B>" + cell.getTaxon().getPublicationName() +
							"</B> <BR>in the Phenoscape KB.</U></FONT></P></HTML>");
				} else if (state.getLabel().equals("absent")) {
					kbURL = absenceLink.replaceFirst("%%TAXONURI%%", taxonURL).replaceFirst("%%ENTITYURI%%", entityURL);
					button.setText("<HTML><P style=\"text-align: center\"><FONT color=\"#000099\"><U>Open browser to view states supporting <BR> <B>absence</B> of <BR><B>" + 
							cell.getCharacter().getLabel() + 
							"</B> in <B>" + cell.getTaxon().getPublicationName() +
							"</B> <BR>in the Phenoscape KB.</U></FONT></P></HTML>");
				} else {
					kbURL = "";
				}
				button.setHorizontalAlignment(SwingConstants.CENTER);
				button.setBorderPainted(false);
				button.setOpaque(false);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							open(new URI(kbURL));
						} catch (URISyntaxException e1) {
							log().error("Bad URL", e1);
						}
					}
				});
				this.panel.add(button);
			}
		}
		this.panel.repaint();
		this.panel.validate();
	}

	private Set<State> stateValuesForCell(MatrixCell cell) {
		final State state = this.getController().getDataSet()
				.getStateForTaxon(cell.getTaxon(), cell.getCharacter());
		final Set<State> states;
		if (state instanceof MultipleState) {
			states = ((MultipleState) state).getStates();
		} else if (state == null) {
			states = Collections.emptySet();
		} else {
			states = Collections.singleton(state);
		}
		return states;
	}

	private class CellSelectionListener implements MatrixCellSelectionListener {

		@Override
		public void matrixCellWasSelected(MatrixCell cell, PhenexController controller) {
			log().debug("Selected: " + cell);
			clearInterface();
			if (cell != null) {
				displaySupportForCell(cell);
			} else {
				panel.removeAll();
				panel.validate();
			}
		}

	}

}
