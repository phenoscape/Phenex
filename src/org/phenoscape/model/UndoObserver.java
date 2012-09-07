package org.phenoscape.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.obo.app.controller.UndoController;
import org.obo.app.model.Observer;
import org.obo.app.model.PropertyChangeObject;

public class UndoObserver {

	private final UndoController undoController;
	private final DataSetObserver dataSetObserver = new DataSetObserver();
	// should this logic be in this class or UndoController?
	private boolean undoing = false;

	public UndoObserver(UndoController controller) {
		this.undoController = controller;
	}

	public void setDataSet(DataSet data) {
		this.dataSetObserver.startObserving(data);
	}

	private class DataSetObserver implements Observer<DataSet> {

		private final PropertyChangeListener publicationListener = new PropertyUndoer("Edit Publication");
		private final PropertyChangeListener pubNotesListener = new PropertyUndoer("Edit Publication Notes");
		private final PropertyChangeListener curatorsListener = new PropertyUndoer("Edit Curators");
		private final PropertyChangeListener matrixListener = new MatrixCellUndoer();
		private final TaxonObserver taxonObserver = new TaxonObserver();
		private final ListObserver<Taxon> taxaObserver = new ListObserver<Taxon>(this.taxonObserver, "Taxon");
		private final CharacterObserver characterObserver = new CharacterObserver();
		private final ListObserver<Character> charactersObserver = new ListObserver<Character>(this.characterObserver, "Character");

		@Override
		public void startObserving(DataSet data) {
			data.addPropertyChangeListener(DataSet.PUBLICATION, this.publicationListener);
			data.addPropertyChangeListener(DataSet.PUBLICATION_NOTES, this.pubNotesListener);
			data.addPropertyChangeListener(DataSet.CURATORS, this.curatorsListener);
			data.addPropertyChangeListener(DataSet.MATRIX_CELL, this.matrixListener);
			this.taxaObserver.startObserving(data.getTaxa());
			this.charactersObserver.startObserving(data.getCharacters());
		}

		@Override
		public void startObserving(Collection<DataSet> objects) {
			for (DataSet data : objects) {
				this.startObserving(data);
			}
		}

		@Override
		public void stopObserving(DataSet data) {
			data.removePropertyChangeListener(DataSet.PUBLICATION, this.publicationListener);
			data.removePropertyChangeListener(DataSet.PUBLICATION_NOTES, this.pubNotesListener);
			data.removePropertyChangeListener(DataSet.CURATORS, this.curatorsListener);
			data.removePropertyChangeListener(DataSet.MATRIX_CELL, this.matrixListener);
			this.taxaObserver.stopObserving(data.getTaxa());
			this.charactersObserver.stopObserving(data.getCharacters());
		}

		@Override
		public void stopObserving(Collection<DataSet> objects) {
			for (DataSet data : objects) {
				this.stopObserving(data);
			}
		}

		private class MatrixCellUndoer implements PropertyChangeListener {

			@Override
			public void propertyChange(final PropertyChangeEvent change) {
				if (undoing) {
					undoing = false;
					return; 
				}
				postEdit(new AbstractUndoableEdit() {

					@Override
					public String getPresentationName() {
						return "Edit Matrix Cell";
					}

					@Override
					public void redo() throws CannotRedoException {
						super.redo();
						undoing = true;
						final MatrixCellValue newValue = (MatrixCellValue)(change.getNewValue());
						((DataSet)(change.getSource())).setStateForTaxon(newValue.getTaxon(), newValue.getCharacter(), newValue.getState());                        
					}

					@Override
					public void undo() throws CannotUndoException {
						super.undo();
						undoing = true;
						final MatrixCellValue oldValue = (MatrixCellValue)(change.getOldValue());
						((DataSet)(change.getSource())).setStateForTaxon(oldValue.getTaxon(), oldValue.getCharacter(), oldValue.getState());
					}

				});
			}

		}

	}

	private class TaxonObserver implements Observer<Taxon> {

		private final PropertyChangeListener validNameListener = new PropertyUndoer("Edit Valid Taxon");
		private final PropertyChangeListener pubNameListener = new PropertyUndoer("Edit Publication Taxon");
		private final PropertyChangeListener matrixTaxonListener = new PropertyUndoer("Edit Matrix Taxon");
		private final PropertyChangeListener commentListener = new PropertyUndoer("Edit Taxon Comment");
		private final PropertyChangeListener figureListener = new PropertyUndoer("Edit Taxon Figure");
		private final SpecimenObserver specimenObserver = new SpecimenObserver();
		private final ListObserver<Specimen> specimensObserver = new ListObserver<Specimen>(this.specimenObserver, "Specimen");

		@Override
		public void startObserving(Taxon taxon) {
			taxon.addPropertyChangeListener(Taxon.VALID_NAME, this.validNameListener);
			taxon.addPropertyChangeListener(Taxon.MATRIX_TAXON_NAME, this.matrixTaxonListener);
			taxon.addPropertyChangeListener(Taxon.PUBLICATION_NAME, this.pubNameListener);
			taxon.addPropertyChangeListener(Taxon.COMMENT, this.commentListener);
			taxon.addPropertyChangeListener(Taxon.FIGURE, this.figureListener);
			this.specimensObserver.startObserving(taxon.getSpecimens());
		}

		@Override
		public void startObserving(Collection<Taxon> objects) {
			for (Taxon taxon : objects) {
				this.startObserving(taxon);
			}
		}

		@Override
		public void stopObserving(Taxon taxon) {
			taxon.removePropertyChangeListener(Taxon.VALID_NAME, this.validNameListener);
			taxon.removePropertyChangeListener(Taxon.MATRIX_TAXON_NAME, this.matrixTaxonListener);
			taxon.removePropertyChangeListener(Taxon.PUBLICATION_NAME, this.pubNameListener);
			taxon.removePropertyChangeListener(Taxon.COMMENT, this.commentListener);
			taxon.removePropertyChangeListener(Taxon.FIGURE, this.figureListener);
			this.specimensObserver.stopObserving(taxon.getSpecimens());
		}

		@Override
		public void stopObserving(Collection<Taxon> objects) {
			for (Taxon taxon : objects) {
				this.stopObserving(taxon);
			}
		}

	}

	private class SpecimenObserver implements Observer<Specimen> {

		private final PropertyChangeListener collectionCodeListener = new PropertyUndoer("Edit Collection Code");
		private final PropertyChangeListener catalogIDListener = new PropertyUndoer("Edit Catalog ID");
		private final PropertyChangeListener commentListener = new PropertyUndoer("Edit Specimen Comment");

		@Override
		public void startObserving(Specimen object) {
			object.addPropertyChangeListener(Specimen.COLLECTION_CODE, this.collectionCodeListener);
			object.addPropertyChangeListener(Specimen.CATALOG_ID, this.catalogIDListener);
			object.addPropertyChangeListener(Specimen.COMMENT, commentListener);
		}

		@Override
		public void startObserving(Collection<Specimen> objects) {
			for (Specimen specimen : objects) {
				this.startObserving(specimen);
			}
		}

		@Override
		public void stopObserving(Specimen object) {
			object.removePropertyChangeListener(Specimen.COLLECTION_CODE, this.collectionCodeListener);
			object.removePropertyChangeListener(Specimen.CATALOG_ID, this.catalogIDListener);
			object.removePropertyChangeListener(Specimen.COMMENT, this.commentListener);
		}

		@Override
		public void stopObserving(Collection<Specimen> objects) {
			for (Specimen specimen : objects) {
				this.stopObserving(specimen);
			}
		}

	}

	private class CharacterObserver implements Observer<Character> {

		private final PropertyChangeListener labelListener = new PropertyUndoer("Edit Character Description");
		private final PropertyChangeListener commentListener = new PropertyUndoer("Edit Character Comment");
		private final PropertyChangeListener figureListener = new PropertyUndoer("Edit Character Figure");
		private final PropertyChangeListener discussionListener = new PropertyUndoer("Edit Character Discussion");
		private final StateObserver stateObserver = new StateObserver();
		private final ListObserver<State> statesObserver = new ListObserver<State>(stateObserver, "State");

		@Override
		public void startObserving(Character character) {
			character.addPropertyChangeListener(Character.LABEL, this.labelListener);
			character.addPropertyChangeListener(Character.COMMENT, this.commentListener);
			character.addPropertyChangeListener(Character.FIGURE, this.figureListener);
			character.addPropertyChangeListener(Character.DISCUSSION, this.discussionListener);
			this.statesObserver.startObserving(character.getStates());
		}

		@Override
		public void startObserving(Collection<Character> characters) {
			for (Character character : characters) {
				this.startObserving(character);
			}
		}

		@Override
		public void stopObserving(Character character) {
			character.removePropertyChangeListener(Character.LABEL, this.labelListener);
			character.removePropertyChangeListener(Character.COMMENT, this.commentListener);
			character.removePropertyChangeListener(Character.FIGURE, this.figureListener);
			character.removePropertyChangeListener(Character.DISCUSSION, this.discussionListener);
			this.statesObserver.stopObserving(character.getStates());
		}

		@Override
		public void stopObserving(Collection<Character> characters) {
			for (Character character : characters) {
				this.stopObserving(character);
			}
		}

	}

	private class StateObserver implements Observer<State> {

		private final PropertyChangeListener labelListener = new PropertyUndoer("Edit State Description");
		private final PropertyChangeListener symbolListener = new PropertyUndoer("Edit State Symbol");
		private final PropertyChangeListener commentListener = new PropertyUndoer("Edit State Comment");
		private final PropertyChangeListener figureListener = new PropertyUndoer("Edit State Figure");
		private final PhenotypeObserver phenotypeObserver = new PhenotypeObserver();
		private final ListObserver<Phenotype> phenotypesObserver = new ListObserver<Phenotype>(this.phenotypeObserver, "Phenotype");

		@Override
		public void startObserving(State state) {
			state.addPropertyChangeListener(State.LABEL, this.labelListener);
			state.addPropertyChangeListener(State.SYMBOL, this.symbolListener);
			state.addPropertyChangeListener(State.COMMENT, this.commentListener);
			state.addPropertyChangeListener(State.FIGURE, this.figureListener);
			this.phenotypesObserver.startObserving(state.getPhenotypes());
		}

		@Override
		public void startObserving(Collection<State> states) {
			for (State state : states) {
				this.startObserving(state);
			}
		}

		@Override
		public void stopObserving(State state) {
			state.removePropertyChangeListener(State.LABEL, this.labelListener);
			state.removePropertyChangeListener(State.SYMBOL, this.symbolListener);
			state.removePropertyChangeListener(State.COMMENT, this.commentListener);
			state.removePropertyChangeListener(State.FIGURE, this.figureListener);
			this.phenotypesObserver.stopObserving(state.getPhenotypes());
		}

		@Override
		public void stopObserving(Collection<State> states) {
			for (State state : states) {
				this.stopObserving(state);
			}
		}

	}

	private class PhenotypeObserver implements Observer<Phenotype> {

		private final PropertyChangeListener entityListener = new PropertyUndoer("Edit Entity");
		private final PropertyChangeListener qualityListener = new PropertyUndoer("Edit Quality");
		private final PropertyChangeListener relatedEntityListener = new PropertyUndoer("Edit Related Entity");
		private final PropertyChangeListener countListener = new PropertyUndoer("Edit Count");
		private final PropertyChangeListener measurementListener = new PropertyUndoer("Edit Measurement");
		private final PropertyChangeListener unitListener = new PropertyUndoer("Edit Unit");
		private final PropertyChangeListener commentListener = new PropertyUndoer("Edit Phenotype Comment");

		@Override
		public void startObserving(Phenotype phenotype) {
			phenotype.addPropertyChangeListener(Phenotype.ENTITY, this.entityListener);
			phenotype.addPropertyChangeListener(Phenotype.QUALITY, this.qualityListener);
			phenotype.addPropertyChangeListener(Phenotype.RELATED_ENTITY, this.relatedEntityListener);
			phenotype.addPropertyChangeListener(Phenotype.COUNT, this.countListener);
			phenotype.addPropertyChangeListener(Phenotype.MEASUREMENT, this.measurementListener);
			phenotype.addPropertyChangeListener(Phenotype.UNIT, this.unitListener);
			phenotype.addPropertyChangeListener(Phenotype.COMMENT, this.commentListener);
		}

		@Override
		public void startObserving(Collection<Phenotype> phenotypes) {
			for (Phenotype phenotype : phenotypes) {
				this.startObserving(phenotype);
			}
		}

		@Override
		public void stopObserving(Phenotype phenotype) {
			phenotype.removePropertyChangeListener(Phenotype.ENTITY, this.entityListener);
			phenotype.removePropertyChangeListener(Phenotype.QUALITY, this.qualityListener);
			phenotype.removePropertyChangeListener(Phenotype.RELATED_ENTITY, this.relatedEntityListener);
			phenotype.removePropertyChangeListener(Phenotype.COUNT, this.countListener);
			phenotype.removePropertyChangeListener(Phenotype.MEASUREMENT, this.measurementListener);
			phenotype.removePropertyChangeListener(Phenotype.UNIT, this.unitListener);
			phenotype.removePropertyChangeListener(Phenotype.COMMENT, this.commentListener);
		}

		@Override
		public void stopObserving(Collection<Phenotype> phenotypes) {
			for (Phenotype phenotype : phenotypes) {
				this.stopObserving(phenotype);
			}
		}

	}

	private class ListObserver<Y extends PropertyChangeObject> {

		private final Observer<Y> elementObserver;
		private final ListListener listListener = new ListListener();
		private final String elementUndoName;

		public ListObserver(Observer<Y> elementObserver, String elementUndoName) {
			this.elementObserver = elementObserver;
			this.elementUndoName = elementUndoName;
		}

		public void startObserving(ObservableList<Y> list) {
			list.addObservableListListener(listListener);
			elementObserver.startObserving(list);
		}

		public void stopObserving(ObservableList<Y> list) {
			list.removeObservableListListener(listListener);
			elementObserver.stopObserving(list);
		}

		@SuppressWarnings("unchecked")
		private class ListListener implements ObservableListListener {

			@Override
			public void listElementPropertyChanged(@SuppressWarnings("rawtypes") ObservableList list, int index) {}

			@Override
			public void listElementReplaced(@SuppressWarnings("rawtypes") ObservableList list, int index, Object oldElement) {
				elementObserver.stopObserving((Y)oldElement);
				elementObserver.startObserving((Y)(list.get(index)));
				log().debug("Replaced " + elementUndoName); //TODO UNDO
			}

			@Override
			public void listElementsAdded(@SuppressWarnings("rawtypes") final ObservableList list, final int index, final int length) {
				final List<Y> added = list.subList(index, index + length);
				elementObserver.startObserving(added);
				if (undoing) {
					undoing = false;
					return; 
				}
				postEdit(new AbstractUndoableEdit() {

					private final List<Y> items = new ArrayList<Y>(added);
					final int location = index;

					@Override
					public String getPresentationName() {
						return "Add " + elementUndoName;
					}

					@Override
					public void redo() throws CannotRedoException {
						super.redo();
						undoing = true;
						list.addAll(this.location, this.items);
					}

					@Override
					public void undo() throws CannotUndoException {
						super.undo();
						undoing = true;
						list.removeAll(this.items);
					}

				});
			}

			@Override
			public void listElementsRemoved(@SuppressWarnings("rawtypes") final ObservableList list, final int index, @SuppressWarnings("rawtypes") final List oldElements) {
				elementObserver.stopObserving(oldElements);
				if (undoing) {
					undoing = false;
					return; 
				}
				postEdit(new AbstractUndoableEdit() {

					final List<Y> items = new ArrayList<Y>(oldElements);
					final int location = index;

					@Override
					public String getPresentationName() {
						return "Delete " + elementUndoName;
					}

					@Override
					public void redo() throws CannotRedoException {
						super.redo();
						undoing = true;
						list.removeAll(this.items);
					}

					@Override
					public void undo() throws CannotUndoException {
						super.undo();
						undoing = true;
						list.addAll(this.location, this.items);
					}

				});
			}

		}

	}

	private class PropertyUndoer implements PropertyChangeListener {

		private final String undoText;

		public PropertyUndoer(String presentationName) {
			this.undoText = presentationName;
		}

		@Override
		public void propertyChange(final PropertyChangeEvent change) {
			if (undoing) {
				undoing = false;
				return; 
			}
			postEdit(new AbstractUndoableEdit() {

				@Override
				public String getPresentationName() {
					return undoText;
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					undoing = true;
					((PropertyChangeObject)(change.getSource())).putValue(change.getPropertyName(), change.getNewValue());
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					undoing = true;
					((PropertyChangeObject)(change.getSource())).putValue(change.getPropertyName(), change.getOldValue());
				}

			});
		}
	}

	private void postEdit(UndoableEdit e) {
		this.undoController.postEdit(e);
	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
