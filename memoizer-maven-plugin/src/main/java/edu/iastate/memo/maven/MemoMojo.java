package edu.iastate.memo.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
@Mojo(name = "memoize", requiresDependencyResolution = ResolutionScope.TEST)
public class MemoMojo extends AbstractMemoMojo {  }
