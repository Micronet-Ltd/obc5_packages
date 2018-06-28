#Instructions

1. Run the test on your MT5 device. To run the full test, dial \*937\*77\#. To run cradle only, dial \*937\*79\#.
1. Connect the device via USB.
1. Set up the inputs for the test.  `cradle_only_template.csv` and `full_test_template.csv` should be placed in the INPUT directory, along with the language file you want to use.  Use `English.bat` as a guide for new languages.  Set the language near the top of the `MT5_TEST_MAIN.bat` file.
1. From a batch command line, run `MT5_TEST_MAIN.bat`.
1. The test results can be found in the `Results` directory.