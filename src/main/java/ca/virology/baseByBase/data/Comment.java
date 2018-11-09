package ca.virology.baseByBase.data;

import org.biojava.bio.seq.StrandedFeature;

import javax.xml.stream.Location;
import java.awt.*;

/**
 * Created by localadmin on 2017-06-01.
 */
public class Comment {

    Location loc;
    StrandedFeature.Strand strand;
    String name;
    String comment;
    Color fgcolor;
    Color bgcolor;

    public Comment(Location loc, StrandedFeature.Strand strand, String name,
                   String comment, Color fgcolor, Color bgcolor) {
        this.loc = loc;
        this.strand = strand;
        this.name = name;
        this.comment = comment;
        this.fgcolor = fgcolor;
        this.bgcolor = bgcolor;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(int start, int end) {
    }

    public StrandedFeature.Strand getStrand() {
        return strand;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public Color getFgcolor() {
        return fgcolor;
    }

    public Color getBgcolor() {
        return bgcolor;
    }
}
