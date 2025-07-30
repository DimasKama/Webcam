### Common
- Update the protocol version. Mod is not compatible with v3.x.x now
- Use H.264 codec instead of JPEG
- Use Netty for networking instead of Java API
- Add server-side bitrate control
- Add `webcam.broadcast` and `webcam.view` permissions
### Fabric
- Add Advanced settings (Requires Cloth Config)
- Add "Show webcams" setting
- Add "Players' webcams" menu
- Add connection status information to the Webcam menu
- Fix crashes with some webcam devices
### Spigot
- Config is now located in plugins/webcam/ directory instead of config/webcam/
- Fix legacy plugin console warning