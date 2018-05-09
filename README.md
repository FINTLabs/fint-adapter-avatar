# FINT Avatar Adapter (FAA)

## Adapter configuration
| Key | Description | Example |
|-----|-------------|---------|
| fint.adapter.organizations | List of orgIds the adapter handles. | rogfk.no, vaf.no, ofk.no |
| fint.adapter.sse-endpoint | Url to the sse endpoint. | https://play-with-fint-adapter.felleskomponent.no/provider/sse/%s |
| fint.adapter.status-endpoint | Url to the status endpoint. | https://play-with-fint-adapter.felleskomponent.no//provider/status |
| fint.adapter.response-endpoint | Url to the response endpoint. | https://play-with-fint-adapter.felleskomponent.no/provider/response |
| fint.adapter.avatar.basedir | Basedir of image storage | /images |
| fint.adapter.avatar.root | Root url to serve images | http://provider:8091 |

- **[SSE Configuration](https://github.com/FINTlibs/fint-sse#sse-configuration)**
- **[OAuth Configuration](https://github.com/FINTlibs/fint-sse#oauth-configuration)** 
