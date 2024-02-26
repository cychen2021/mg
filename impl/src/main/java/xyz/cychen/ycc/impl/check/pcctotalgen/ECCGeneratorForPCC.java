package xyz.cychen.ycc.impl.check.pcctotalgen;

import xyz.cychen.ycc.framework.Binding;
import xyz.cychen.ycc.framework.Link;
import xyz.cychen.ycc.framework.cct.*;
import xyz.cychen.ycc.framework.check.Generator;
import xyz.cychen.ycc.impl.check.ecc.ECCGenerator;

public class ECCGeneratorForPCC extends ECCGenerator {
    @Override
    public Link generate(CCT cct, Binding binding) {
        Link r = super.generate(cct, binding);
//        setIncrementalLinkCount(cct, r.size());
        return r;
    }
}
