# WxMCPServer MSR Container
# Based on webMethods Microservices Runtime base image

# Use official MSR base image
FROM ibmwebmethods.azurecr.io/webmethods-microservicesruntime:11.1

# Switch to root to copy files and set ownership
USER root

# Copy WxMCPServer package files with proper ownership
COPY --chown=sagadmin:sagadmin ./ /opt/softwareag/IntegrationServer/packages/WxMCPServer/

# Switch back to sagadmin user
USER sagadmin

# Expose MSR ports
EXPOSE 5555 9999

# Start MSR
CMD ["/opt/softwareag/IntegrationServer/bin/startContainer.sh"]