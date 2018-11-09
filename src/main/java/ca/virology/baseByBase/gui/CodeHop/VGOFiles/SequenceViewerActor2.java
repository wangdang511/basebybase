package ca.virology.baseByBase.gui.CodeHop.VGOFiles;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import ca.virology.vgo.gui.SelectionEvent;
import ca.virology.vgo.gui.SequencePanelHolder;
import org.biojava.bio.gui.sequence.SequenceRenderContext;

import java.util.List;


public interface SequenceViewerActor2 {
    List action(SequenceRenderContext var1, int var2, SequencePanelHolder2 var3);

    SelectionEvent2 selection(SequenceRenderContext var1, int var2, boolean var3, SequencePanelHolder2 var4);
}
