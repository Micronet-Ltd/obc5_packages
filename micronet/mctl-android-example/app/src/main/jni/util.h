#include <stdio.h>
#include <ctype.h>

static inline int hex_value(char c)
{
	// TODO: existing function does this? (other than scanf)
	if(!isxdigit(c))
	   return 0; // ignores

	if(isalpha(c))
		return 0xa + (tolower(c) - 'a');

	// all others are 0-9
	return (unsigned)c - '0';
}
