XBDD 
====

[![Build Status](https://travis-ci.org/rvbabilonia/xbdd.svg?branch=master)](https://travis-ci.org/rvbabilonia/xbdd)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8eaadff1b74f42f0b43b92ac55aeb5dd)](https://www.codacy.com/app/rvbabilonia/XBDD?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rvbabilonia/XBDD&amp;utm_campaign=Badge_Grade)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Frvbabilonia%2Fxbdd.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Frvbabilonia%2Fxbdd?ref=badge_shield)

XBDD is a tool to unite automated and manual testing efforts using Cucumber feature files. Test reports can be uploaded to XBDD manually or via a continuous integration server, giving you an information radiator on how many tests are passing, how many failed and how many can be run manually. Pin a report and get your team to walk through the manual feature files and check off each step to perform a full regression test.

See the [user guide](/docs/usage/user-guide.md) for a fuller overview of XBDD functionality.

Installation
------------

See [INSTALL.md](/docs/INSTALL.md) for details on prerequisites, installation and configuration.

For Performance / Production
----------------------------

Run the script contained within `mongoIndexes.txt` to create compound indexes required for search/sort.

Usage
=====

See the [user guide](/docs/usage/user-guide.md) for an overview of XBDD functionality.

Uploading reports
-----------------

Cucumber JSON reports can be uploaded manually from the XBDD main page. See [UPLOAD.md](/docs/UPLOAD.md) for instructions on automating the upload with Maven.

Contributing
============
For all those wishing to contribute to XBDD please fork the repository, and submit your changes via a pull request.
Please ensure your pull request follows the pre-existing coding style and that `mvn clean verify` passes.  You can use `grunt pretty` to format your CSS and JS correctly.


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Frvbabilonia%2Fxbdd.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Frvbabilonia%2Fxbdd?ref=badge_large)