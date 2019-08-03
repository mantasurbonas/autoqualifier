# Spring components auto Qualifier
Creates automatic @Qualifier("..") annotations for Spring @Autowired fields, add names to Spring @Components, @Repositories and @Services.

## Example

Given a Java project having some @Autowired Spring components like this

``` Java

@Service
public class AService{
...
}

@Service
public class BService{
   ...
   @Autowired
   private AService aService;

}

```

In this case Java obfuscators wouldn't work reliably, since the component classes become obfuscated and autowiring fails.

This particular tool recursively walks through Java sources, and modifies .java files like this:

``` Java

@Service("54646546asdfasdf")
public class AService{
...
}

@Service("qwerasdf4654645654")
public class BService{
   ...
   @Autowired @Qualifier("54646546asdfasdf")
   private AService aService;

}

```

Now Proguard obfuscation works properly, and the obfuscated files don't reveal your Spring bean names and fields.

## Notes
Ignores already named Spring components, and does not touch already @ualifier'd fields.  

