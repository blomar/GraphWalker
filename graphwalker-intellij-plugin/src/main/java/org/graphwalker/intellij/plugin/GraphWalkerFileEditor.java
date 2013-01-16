package org.graphwalker.intellij.plugin;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

public class GraphWalkerFileEditor extends UserDataHolderBase implements FileEditor {

    @NonNls private static final String NAME = "GraphWalker Editor";

    private static final List<GraphWalkerFileEditor> ourEditors = new LinkedList<GraphWalkerFileEditor>();

    private final VirtualFile myFile;
    private final GraphWalkerFileEditorComponent myEditorComponent;

    public GraphWalkerFileEditor(@NotNull Project project, @NotNull VirtualFile file) {
        myFile = file;

        myEditorComponent = new GraphWalkerFileEditorComponent();
        ourEditors.add(this);
    }

    @NotNull
    public JComponent getComponent() {
        return myEditorComponent.getComponent();
    }

    public JComponent getPreferredFocusedComponent() {
        return myEditorComponent.getPreferredFocusedComponent();
    }

    @NotNull
    public String getName() {
        return NAME;  // TODO: Fix me (Auto generated)
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new FileEditorState() {
            public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
                return false;
            }
        };
    }

    public void setState(@NotNull FileEditorState state) {
    }

    public boolean isModified() {
        return myEditorComponent.isModified();
    }

    public boolean isValid() {
        return myFile.isValid(); // && myEditorComponent.isValidForFormat();
    }

    public void selectNotify() {
    }

    public void deselectNotify() {
    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    public void dispose() {
        // TODO: Fix me (Auto generated)
    }

}