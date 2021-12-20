# GQLFedUtils

## Subcommands

### purge

This subcommand will remove any **type** / **input** / **enum** (and their fields) not annotated with some pattern.

GraphQL **schemas** / **directives** / **scalars** are always kept

To use it you need a configuration file with two properties: `keepPatterns`, `secondKeepPatterns`. Example:

```yaml
keepPatterns:
  - "@GK"
  - "@GateKeep"
  - "@GKeep"

secondKeepPatterns:
  - ADMIN
  - ADMINISTRATOR
  - ADMINS
  - admin
  - admins
```

`secondKeepPatterns` is optional, but it may be useful to add more specificity.

With this configuration, a **type** / **input** / **enum** (and their fields) that is not annotated with first and
second patterns will be removed. Example:

```graphql
"""
This should be removed
"""
type SomeType {
    id: ID!
}

"""
This should not be removed
@GateKeep ADMIN
"""
type AnotherType {
    """
    @GK ADMINISTRATOR
    """
    id: ID!

    """
    This will be removed because it's not annotated
    """
    name: String!
}
```

If you run the purge command on the input above, you'll get the following output:

```graphql
"""
This should not be removed
"""
type AnotherType {
    id: ID!
}
```

Notice that all **type** / **input** / **enum** (and their fields) that weren't annotated were removed, and the
annotation was removed from the ones that do were annotated.

That's basically what this subcommand does. Check [`src/test/resources`](src/test/resources) for more examples.
`*.graphql` are input files, `*.expected.graphql` are output files (the expected output for `purge` subcommand)

### dot

Transpile your graphql schema to dot code.

This is a work in progress, and it's not ready yet.

Just as an interesting note:

This tool uses the default `String#indexOf` because it is really efficient. It's even more efficient than the
implementation of Rabin-Karp's algorithm
and [here](https://stackoverflow.com/questions/9741188/java-indexof-function-more-efficient-than-rabin-karp-search-efficiency-of-text)
is why
