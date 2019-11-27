# Chalkbox

The user/developer guide is here: https://docs.braewebb.com/guides/chalkbox.pdf This guide is intended to help you get Chalkbox installed and ready to run submissions.
Chalkbox is a really big tool which you don't need to understand most of because it works for a lot of courses but the full documentation is here: https://docs.braewebb.com/chalkbox/.
A box file is used to configure how chalkbox processes submissions.

Below is a description of each of the lines of an example box file.  
BlackboardCollector is a built in Chalkbox collector that reads and extracts submissions from a downloaded blackboard submissions zip.  
`collector=chalkbox.collectors.BlackboardCollector`  
CSSE1001Test is a test built to use the 1001 test runner with appropriate flags to get JSON output from the test runner.  
`processor=chalkbox.python.CSSE1001Test`  
SaveJsonOutput is going to store all of the test results in a folder where each student has their own .json file.  
`output=chalkbox.output.SaveJsonOutput`  

The gradebook parameter is the filename of the downloaded blackboard submissions zip.  
`gradebook=2019s2a2-gradebook.zip`  
The folder in which the SaveJsonOutput outputter stores all test results    
`json=a2json`  
Name of the test file (for this assignment it is test_a2.py)  
`runner=test_a2.py`  
Included is a directory that has all its contents included with the students submission when it is tested.  
`included=ass2/queens`

Uploading Chalkbox results into blackboard is currently awkward. Chalkbox was designed for Whiteboard so does not have an in-built blackboard exporter.
There is a hacked together Python script that can upload results to blackboard. It requires modification for each assignment.
Ideally a Chalkbox output module for blackboard could be written.
The script is available as a separate zip file. It needs to be provided with a downloaded blackboard grades CSV file and it will then spit another one out for uploading grades.
