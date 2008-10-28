package org.phenoscape.model;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.phenoscape.app.Observer;
import org.phenoscape.app.PropertyChangeObject;

public class UndoController {
    
    private final UndoableEditSupport undoSupport = new UndoableEditSupport();
    private final UndoManager undoManager = new UndoManager();
    private final DataSetObserver dataSetObserver = new DataSetObserver();
    private final Action undo;
    private final Action redo;
    private boolean undoing = false;
    
    public UndoController() {
        this.undoSupport.addUndoableEditListener(this.undoManager);
        this.undo = new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent e) {
                undoManager.undo();
                updateUndoRedoActions();
            }
        };
        this.redo = new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent e) {
                undoManager.redo();
                updateUndoRedoActions();
            }
        };
        this.updateUndoRedoActions();
    }
    
    public Action getUndoAction() {
        return this.undo;
    }
    
    public Action getRedoAction() {
        return this.redo;
    }
    
    public void setDataSet(DataSet data) {
        this.dataSetObserver.startObserving(data);
    }
    
    private class DataSetObserver implements Observer<DataSet> {
        
        private final PropertyChangeListener publicationListener = new PropertyUndoer("Edit Publication");
        private final PropertyChangeListener pubNotesListener = new PropertyUndoer("Edit Publication Notes");
        private final PropertyChangeListener curatorsListener = new PropertyUndoer("Edit Curators");
        private final TaxonObserver taxonObserver = new TaxonObserver();
        private final ListObserver<Taxon> taxaObserver = new ListObserver<Taxon>(this.taxonObserver, "Taxon");
        private final CharacterObserver characterObserver = new CharacterObserver();
        private final ListObserver<Character> charactersObserver = new ListObserver<Character>(this.characterObserver, "Character");
        
        public void startObserving(DataSet data) {
            data.addPropertyChangeListener(DataSet.PUBLICATION, this.publicationListener);
            data.addPropertyChangeListener(DataSet.PUBLICATION_NOTES, this.pubNotesListener);
            data.addPropertyChangeListener(DataSet.CURATORS, this.curatorsListener);
            this.taxaObserver.startObserving(data.getTaxa());
            this.charactersObserver.startObserving(data.getCharacters());
        }
        
        public void startObserving(Collection<DataSet> objects) {
            for (DataSet data : objects) {
                this.startObserving(data);
            }
        }
        
        public void stopObserving(DataSet data) {
            data.removePropertyChangeListener(DataSet.PUBLICATION, this.publicationListener);
            data.removePropertyChangeListener(DataSet.PUBLICATION_NOTES, this.pubNotesListener);
            data.removePropertyChangeListener(DataSet.CURATORS, this.curatorsListener);
            this.taxaObserver.stopObserving(data.getTaxa());
            this.charactersObserver.stopObserving(data.getCharacters());
        }
        
        public void stopObserving(Collection<DataSet> objects) {
            for (DataSet data : objects) {
                this.stopObserving(data);
            }
        }
        
    }
    
    private class TaxonObserver implements Observer<Taxon> {

        private final PropertyChangeListener validNameListener = new PropertyUndoer("Edit Valid Taxon");
        private final PropertyChangeListener pubNameListener = new PropertyUndoer("Edit Publication Taxon");
        private final PropertyChangeListener matrixTaxonListener = new PropertyUndoer("Edit Matrix Taxon");
        private final PropertyChangeListener commentListener = new PropertyUndoer("Edit Taxon Comment");
        private final SpecimenObserver specimenObserver = new SpecimenObserver();
        private final ListObserver<Specimen> specimensObserver = new ListObserver<Specimen>(this.specimenObserver, "Specimen");

        public void startObserving(Taxon taxon) {
            taxon.addPropertyChangeListener(Taxon.VALID_NAME, this.validNameListener);
            taxon.addPropertyChangeListener(Taxon.MATRIX_TAXON_NAME, this.matrixTaxonListener);
            taxon.addPropertyChangeListener(Taxon.PUBLICATION_NAME, this.pubNameListener);
            taxon.addPropertyChangeListener(Taxon.COMMENT, this.commentListener);
            this.specimensObserver.startObserving(taxon.getSpecimens());
        }
        
        public void startObserving(Collection<Taxon> objects) {
            for (Taxon taxon : objects) {
                this.startObserving(taxon);
            }
        }

        public void stopObserving(Taxon taxon) {
            taxon.removePropertyChangeListener(Taxon.VALID_NAME, this.validNameListener);
            taxon.removePropertyChangeListener(Taxon.MATRIX_TAXON_NAME, this.matrixTaxonListener);
            taxon.removePropertyChangeListener(Taxon.PUBLICATION_NAME, this.pubNameListener);
            taxon.removePropertyChangeListener(Taxon.COMMENT, this.commentListener);
            this.specimensObserver.stopObserving(taxon.getSpecimens());
        }
        
        public void stopObserving(Collection<Taxon> objects) {
            for (Taxon taxon : objects) {
                this.stopObserving(taxon);
            }
        }

    }
    
    private class SpecimenObserver implements Observer<Specimen> {
        
        private final PropertyChangeListener collectionCodeListener = new PropertyUndoer("Edit Collection Code");
        private final PropertyChangeListener catalogIDListener = new PropertyUndoer("Edit Catalog ID");

        public void startObserving(Specimen object) {
            object.addPropertyChangeListener(Specimen.COLLECTION_CODE, this.collectionCodeListener);
            object.addPropertyChangeListener(Specimen.CATALOG_ID, this.catalogIDListener);
        }

        public void startObserving(Collection<Specimen> objects) {
            for (Specimen specimen : objects) {
                this.startObserving(specimen);
            }
        }

        public void stopObserving(Specimen object) {
            object.removePropertyChangeListener(Specimen.COLLECTION_CODE, this.collectionCodeListener);
            object.removePropertyChangeListener(Specimen.CATALOG_ID, this.catalogIDListener);
        }

        public void stopObserving(Collection<Specimen> objects) {
            for (Specimen specimen : objects) {
                this.stopObserving(specimen);
            }
        }
        
    }
    
    private class CharacterObserver implements Observer<Character> {
        
        private final PropertyChangeListener labelListener = new PropertyUndoer("Edit Character Description");
        private final PropertyChangeListener commentListener = new PropertyUndoer("Edit Character Comment");
        private final StateObserver stateObserver = new StateObserver();
        private final ListObserver<State> statesObserver = new ListObserver<State>(stateObserver, "State");

        public void startObserving(Character character) {
            character.addPropertyChangeListener(Character.LABEL, this.labelListener);
            character.addPropertyChangeListener(Character.COMMENT, this.commentListener);
            this.statesObserver.startObserving(character.getStates());
        }

        public void startObserving(Collection<Character> characters) {
            for (Character character : characters) {
                this.startObserving(character);
            }
        }

        public void stopObserving(Character character) {
            character.removePropertyChangeListener(Character.LABEL, this.labelListener);
            character.removePropertyChangeListener(Character.COMMENT, this.commentListener);
            this.statesObserver.stopObserving(character.getStates());
        }

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
        private final PhenotypeObserver phenotypeObserver = new PhenotypeObserver();
        private final ListObserver<Phenotype> phenotypesObserver = new ListObserver<Phenotype>(this.phenotypeObserver, "Phenotype");
        
        public void startObserving(State state) {
            state.addPropertyChangeListener(State.LABEL, this.labelListener);
            state.addPropertyChangeListener(State.SYMBOL, this.symbolListener);
            state.addPropertyChangeListener(State.COMMENT, this.commentListener);
            this.phenotypesObserver.startObserving(state.getPhenotypes());
        }

        public void startObserving(Collection<State> states) {
            for (State state : states) {
                this.startObserving(state);
            }
        }

        public void stopObserving(State state) {
            state.removePropertyChangeListener(State.LABEL, this.labelListener);
            state.removePropertyChangeListener(State.SYMBOL, this.symbolListener);
            state.removePropertyChangeListener(State.COMMENT, this.commentListener);
            this.phenotypesObserver.stopObserving(state.getPhenotypes());
        }

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
        
        public void startObserving(Phenotype phenotype) {
            phenotype.addPropertyChangeListener(Phenotype.ENTITY, this.entityListener);
            phenotype.addPropertyChangeListener(Phenotype.QUALITY, this.qualityListener);
            phenotype.addPropertyChangeListener(Phenotype.RELATED_ENTITY, this.relatedEntityListener);
            phenotype.addPropertyChangeListener(Phenotype.COUNT, this.countListener);
            phenotype.addPropertyChangeListener(Phenotype.MEASUREMENT, this.measurementListener);
            phenotype.addPropertyChangeListener(Phenotype.UNIT, this.unitListener);
            phenotype.addPropertyChangeListener(Phenotype.COMMENT, this.commentListener);
        }

        public void startObserving(Collection<Phenotype> phenotypes) {
            for (Phenotype phenotype : phenotypes) {
                this.startObserving(phenotype);
            }
        }

        public void stopObserving(Phenotype phenotype) {
            phenotype.removePropertyChangeListener(Phenotype.ENTITY, this.entityListener);
            phenotype.removePropertyChangeListener(Phenotype.QUALITY, this.qualityListener);
            phenotype.removePropertyChangeListener(Phenotype.RELATED_ENTITY, this.relatedEntityListener);
            phenotype.removePropertyChangeListener(Phenotype.COUNT, this.countListener);
            phenotype.removePropertyChangeListener(Phenotype.MEASUREMENT, this.measurementListener);
            phenotype.removePropertyChangeListener(Phenotype.UNIT, this.unitListener);
            phenotype.removePropertyChangeListener(Phenotype.COMMENT, this.commentListener);
        }

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

            public void listElementPropertyChanged(ObservableList list, int index) {}

            public void listElementReplaced(ObservableList list, int index, Object oldElement) {
                elementObserver.stopObserving((Y)oldElement);
                elementObserver.startObserving((Y)(list.get(index)));
                log().debug("Replaced " + elementUndoName); //TODO UNDO
            }

            public void listElementsAdded(final ObservableList list, final int index, final int length) {
                final List added = list.subList(index, index + length);
                elementObserver.startObserving(added);
                log().debug("Added " + elementUndoName);
                if (undoing) {
                    undoing = false;
                    return; 
                }
                postEdit(new AbstractUndoableEdit() {
                    
                    private final List items = new ArrayList(added);
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

            public void listElementsRemoved(final ObservableList list, final int index, final List oldElements) {
                elementObserver.stopObserving(oldElements);
                if (undoing) {
                    undoing = false;
                    return; 
                }
                postEdit(new AbstractUndoableEdit() {
                    
                    final List items = new ArrayList(oldElements);
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
        this.undoSupport.postEdit(e);
        this.updateUndoRedoActions();
    }
    
    private void updateUndoRedoActions() {
        this.undo.setEnabled(this.undoManager.canUndo());
        this.undo.putValue(Action.NAME, this.undoManager.getUndoPresentationName());
        this.redo.setEnabled(this.undoManager.canRedo());
        this.redo.putValue(Action.NAME, this.undoManager.getRedoPresentationName());
    }
    
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
