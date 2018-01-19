# s3cmd-plugin
Dockstore s3cmd file provisioning plugin.  Requires s3cmd installed along with a valid configuration file.
Run s3cmd --configure to create a configuration file at the default location.

## Notes
For files over 150 GB, chunk size is kept as close to the default chunk size as possible.  Files over 50 TB is unsupported.

## Usage

The s3cmd plugin is capable of downloading files by calling out to an installed copy of the [s3cmd client](http://s3tools.org/s3cmd).

```
$ cat test.s3cmd.json
{
  "input_file": {
        "class": "File",
        "path": "s3cmd://bucket/dir/file.txt"
    },
    "output_file": {
        "class": "File",
        "path": "s3cmd://bucket/dir/file2.txt"
    }
}

$ dockstore tool launch --entry  quay.io/briandoconnor/dockstore-tool-md5sum  --json test.s3cmd.json
Creating directories for run of Dockstore launcher at: ./datastore//launcher-2ebce330-2a44-4a3a-9d6d-55c152a5c38e
Provisioning your input files to your local machine
Downloading: #input_file from s3cmd://bucket/dir/file.txt into directory: /home/gluu/md5/./datastore/launcher-9e1ec2cb-2315-4487-9afe-7f4d9179fcd6/inputs/a356d1c3-f095-4801-8ef7-f0e429e0316e
Calling on plugin io.dockstore.provision.S3CmdPlugin$S3CmdProvision to provision s3cmd://bucket/dir/file.txt
...
Uploading: #output_file from /home/gluu/md5/./datastore/launcher-9e1ec2cb-2315-4487-9afe-7f4d9179fcd6/outputs/md5sum.txt to : s3cmd://bucket/dir/file2.txt
Calling on plugin io.dockstore.provision.S3CmdPlugin$S3CmdProvision to provision to s3cmd://bucket/dir/file2.txt
```


## Configuration

Download the plugin with `dockstore plugin download` by adding an entry to ~/.dockstore/plugins.json similar to the following
```
[
  {
    "name": "dockstore-file-s3cmd-plugin",
    "version": "0.0.4"
  },
  {
    "name": "dockstore-file-s3-plugin",
    "version": "0.0.3"
  },
  {
    "name": "dockstore-file-synapse-plugin",
    "version": "0.0.5"
  }
]
```

This plugin gets configuration information from the following structure in ~/.dockstore/config

```
[dockstore-file-s3cmd-plugin]
client = /usr/bin/s3cmd
config-file-location = /home/user/.s3cfg
verbosity = Normal
```

Set the client location to your own s3cmd client and also make sure that the configuration file is available at the config-file-location.
Verbosity configuration lets you configure the level of info display.  The default is 'Normal'.

### Verbosity
- Level "Minimal" doesn't print the `s3cmd mb` stdout/stderr
- Level "Normal" prints the `s3cmd mb` stdout/stderr

Using the `--debug` flag with the dockstore client will print out the commands executed by this plugin and `s3cmd info` stdout/stderr
```
$ ./dockstore --debug tool launch --local-entry Dockstore.cwl --json test.json
15:23:40.486 [main] DEBUG io.dockstore.provision.S3CmdPlugin - Executing command: /usr/local/bin/s3cmd -c /home/gluu/.s3cfg info s3://dockstore.temp
```
`s3cmd put` and `s3cmd get` stdout/stderr is always displayed no matter what

In summary, the following is displayed by default:
- `s3cmd put`
- `s3cmd get`
- `s3cmd mb`

In summary, the following is not displayed by default:
- the command being executed
- `s3cmd info`
