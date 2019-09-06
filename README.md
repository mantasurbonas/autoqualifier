# Spring components auto Qualifier
Manages @Qualifier("..") annotations for Spring @Autowired fields add names of Spring @Components, @Repositories and @Services.

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

This particular Maven plugin recursively walks through Java sources, and modifies .java files like this:

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

## Configuration
This Maven config snipplet enforces random names on all @Components and all the matching Qualifier("...")s on @Autowired fields:

``` xml
<plugins>
	<plugin>
		<groupId>lt.visma.javahub</groupId>
		<artifactId>autoqualifier-maven-plugin</artifactId>
		<executions><execution><phase>process-sources</phase><goals><goal>autoqualifier</goal></goals></execution></executions>
		<configuration>
			<mode>nameRandomly</mode>
		</configuration>
	</plugin>
</plugins>

```

Supported modes are:
  * **ignore** (do nothing)
  * **log** (logs all components and autowired fields found)
  * **warnUnnamed** (warn if some components are unnamed, or some autowired fields are unqualified - don't touch source files)
  * **warnNamed** (warn if some components are named, or some autowired fields are qualified - don't touch source files)
  * **errorUnnamed** (log error and stop build process if some components are unnamed, or some autowired fields are unqualified - don't touch source files)
  * **errorNamed** (log error and stop build process if some components are named, or some autowired fields are qualified - don't touch source files)
  * **nameByClass** ( automatically name components and qualify autowired fields by dependency classname - update source files where necessary)
  * **nameRandomly** (	automatically name components and qualify autowired fields by random md5 hash  - update source files where necessary)
  * **unname** (remove names from components and autowired fields - update source files where necessary)

