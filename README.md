# tumblr-tag-generator

A generator of Tumblr blogs based on a tag to search for

## Installation

Download from https://github.com/palfrey/tumblr-tag-generator. Make sure you've got
[Leiningen](http://leiningen.org/) installed.

## Usage

Copy config.edn.example to config.edn, edit appropriately, and then do `lein run`.
First time you do this it'll spawn a browser so we can get the OAuth tokens and
rewrite the config.edn to store the tokens. After that, it'll post any missing items
from the top twenty posts.

### Bugs

Only gets the first 20 posts...

## License

Copyright (C) 2014 Tom Parker

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
