Contributing to Mantle
======================

We love to see new ideas realised by people, however as a shared library we need to demand pretty high standards for all contributions.
**Failure to adhere to these can result in your PRs being rejected!**

## Coding Style
- We use Scala for **all** classes within Mantle. This is a single-language environment. Java code will be rejected.
- Scaladocs (much like Javadocs in form) must be provided for all methods, with the following:
    - One-line summary of method purpose/function
    - Optionally, a detailed paragraph about the function; limitations, known issues, how to use safely etc should be in @note tags.
    - @example tag if needed to clarify use
    - @param tag per parameter
    - @returns tag for non-void methods
- Scaladocs must also be provided for classes.
    - One-line description of class e.g. `Mantle-Core object repository`
    - More detailed description of what the class is for
    - @constructor and @param as appropriate
    - @author tag in the form `@author GitHubUsername <emailaddr@example.com>` (Note that the email section can be dropped if you feel the need)
    - Additional authors can be credited in additional @author tags.
        - Don't add yourself to @author if you just made a small change; the tag is intended for the **original** writers.
- Scaladocs sit **above** any annotations on the class/method
- Do not provide Scaladocs for variables! Just name them something logical and not too long.
- Use **2-space** tabs (this is recommended form for all Scala projects)
- Use immutable `val` entries instead of `var` where possible
- Only `val` objects should be public exposed; `var` objects must be private and hidden behind getters/setters.
- Ideally, all access should be as restrictive as possible;
    - `var` values must **always** be private
        - Getters/setters follow normal method rules
    - `val` value should be private if only used internally, protected if only within the package and public if it's world-readable (or needed outside the package)
    - Methods should have the minimum access required to be useful (private for internal, protected for intra-package/subclasses, public for world)
- Variable names are in lower camelCase - An upper character for the start of every word except the first.
- Use of Functional Programming idioms is recommended, but not necessary (not everyone has been trained in this, and we acknowledge that)
    - In particular, I (Sunstrike) would highly recommend atleast getting to grasps with the functions `map`, `filter` and `fold` in general (Scala or otherwise)
    - Also remember that *functions are data* in FP. We can pass functions around just like values. This may be used in idiomatic code.

Perhaps most importantly out of all this, please **apply common sense** -- If you think your code looks a mess, it probably is! Tidy up and when you're happy with it, send us a PR and we'll give feedback if it's in a state we won't pull.

## IntelliJ IDEA
In the root of the repository, an IDEA settings jar is provided. It contains the SlimeKnights code formatting settings. The only deviation from this should be a newline before the closing brace of a class!
